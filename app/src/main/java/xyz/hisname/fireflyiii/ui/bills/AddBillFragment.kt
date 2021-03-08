/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.bills

import android.content.res.ColorStateList
import android.graphics.Color.rgb
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import me.toptas.fancyshowcase.FancyShowCaseQueue
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentAddBillBinding
import xyz.hisname.fireflyiii.ui.markdown.MarkdownViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyBottomSheetViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File
import java.util.*

class AddBillFragment: BaseAddObjectFragment() {

    private var billAttribute: BillAttributes? = null
    private var notes: String? = null
    private var repeatFreq: String = ""
    private var currency = ""
    private val billId by lazy { arguments?.getLong("billId") ?: 0 }
    private var billDescription: String? = ""
    private lateinit var queue: FancyShowCaseQueue
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val billViewModel by lazy { getImprovedViewModel(AddBillViewModel::class.java) }
    private lateinit var fileUri: Uri
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }
    private var fragmentAddBillBinding: FragmentAddBillBinding? = null
    private val binding get() = fragmentAddBillBinding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAddBillBinding = FragmentAddBillBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(binding.dialogAddBillLayout)
        updateEditText()
        showHelpText()
        setFab()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0))
                attachmentItemAdapter.add(fileUri)
                if (billId != 0L){
                    toastInfo("Uploading...")
                    billViewModel.uploadFile(billId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
                        // Only show the updated files array if upload succeeds
                        if(workInfo[0].state == WorkInfo.State.SUCCEEDED){
                            binding.attachmentInformation.adapter?.notifyDataSetChanged()
                            toastSuccess("File uploaded")
                        } else {
                            toastError("There was an issue uploading your file", Toast.LENGTH_LONG)
                        }
                    }
                } else {
                    binding.attachmentInformation.adapter?.notifyDataSetChanged()
                }
            }
        }
        chooseDocument = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){ fileChoosen ->
            if(fileChoosen != null){
                fileChoosen.forEach { file ->
                    attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                            "", Uri.EMPTY, FileUtils.getFileName(requireContext(), file) ?: "",
                            "", "", "", 0, "", "", ""), 0))
                }
                attachmentItemAdapter.addAll(fileChoosen)
                if (billId != 0L){
                    toastInfo("Uploading...")
                    billViewModel.uploadFile(billId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
                        // Only show the updated files array if upload succeeds
                        if(workInfo[0].state == WorkInfo.State.SUCCEEDED){
                            binding.attachmentInformation.adapter?.notifyDataSetChanged()
                            toastSuccess("File uploaded")
                        } else {
                            toastError("There was an issue uploading your file", Toast.LENGTH_LONG)
                        }
                    }
                } else {
                    binding.attachmentInformation.adapter?.notifyDataSetChanged()
                }
            }
        }
    }


    private fun updateEditText(){
        if(billId != 0L){
            billViewModel.getBillById(billId).observe(viewLifecycleOwner) { billData ->
                billAttribute = billData.billAttributes
                binding.descriptionEdittext.setText(billAttribute?.name)
                billDescription = billAttribute?.name
                binding.minAmountEdittext.setText(billAttribute?.amount_min.toString())
                binding.maxAmountEdittext.setText(billAttribute?.amount_max.toString())
                currency = billAttribute?.currency_code ?: ""
                billViewModel.getBillCurrencyDetails(billId).observe(viewLifecycleOwner){ currencyDetails ->
                    binding.currencyEdittext.setText(currencyDetails)
                }
                binding.billDateEdittext.setText(billAttribute?.date.toString())
                binding.skipEdittext.setText(billAttribute?.skip.toString())
                binding.notesEdittext.setText(billAttribute?.notes)
                binding.frequencyExposedDropdown.setText(billAttribute?.repeat_freq?.substring(0, 1)?.toUpperCase()
                        + billAttribute?.repeat_freq?.substring(1))

                // Weird bug where only 1 value will show in the array if I don't use this
                val spinnerAdapter = ArrayAdapter(requireContext(),
                        R.layout.cat_exposed_dropdown_popup_item, resources.getStringArray(R.array.repeat_frequency))
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.frequencyExposedDropdown.setAdapter(spinnerAdapter)
                displayAttachment()
            }
        }
    }

    private fun displayAttachment(){
        billViewModel.billAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        false, { data: AttachmentData ->
                    AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.are_you_sure))
                            .setPositiveButton(android.R.string.ok){ _, _ ->
                                billViewModel.deleteAttachment(data).observe(viewLifecycleOwner){ isSuccessful ->
                                    if(isSuccessful){
                                        attachmentDataAdapter.remove(data)
                                        binding.attachmentInformation.adapter?.notifyDataSetChanged()
                                        toastSuccess("Deleted " + data.attachmentAttributes.filename)
                                    } else {
                                        toastError("There was an issue deleting " + data.attachmentAttributes.filename, Toast.LENGTH_LONG)
                                    }
                                }
                            }
                            .show()
                }) { another: Int -> }
            }
        }
    }

    private fun setFab(){
        if(billId != 0L){
            binding.addBillFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        binding.addBillFab.setOnClickListener {
            hideKeyboard()
            notes = if(binding.notesEdittext.isBlank()){
                null
            } else {
                binding.notesEdittext.getString()
            }
            if(billId == 0L){
                submitData()
            } else {
                updateBill()
            }
        }
    }

    override fun setIcons(){
        binding.currencyEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        binding.minAmountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_dollar_sign
                    colorRes = R.color.md_yellow_A700
                    sizeDp = 16
                },null, null, null)
        binding.maxAmountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_dollar_sign
                    colorRes = R.color.md_yellow_A700
                    sizeDp = 16
                },null, null, null)
        binding.billDateEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_calendar
                    color = colorList(ColorStateList.valueOf(rgb(18, 122, 190)))
                    sizeDp = 24
                },null, null, null)
        binding.skipEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_sort_numeric_up
                    colorRes = R.color.md_red_500
                    sizeDp = 24
                },null, null, null)
        binding.addBillFab.setBackgroundColor(getCompatColor(R.color.colorPrimaryDark))
        binding.addBillFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
        binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
        binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            binding.attachmentInformation.adapter?.notifyDataSetChanged()
        }) { another: Int -> }

    }

    private fun showHelpText(){
        queue = FancyShowCaseQueue()
                .add(showCase(R.string.bills_create_intro, "addBillDescriptionCaseView", binding.appbar))
                .add(showCase(R.string.bills_create_name, "descriptionCaseView", binding.descriptionEdittext))
                .add(showCase(R.string.bills_create_amount_min_holder, "minMaxAmountCaseView",
                        binding.minAmountLayout))
                .add(showCase(R.string.bills_create_skip_holder, "skipCaseView", binding.skipLayout))
                .add(showCase(R.string.bills_create_repeat_freq_holder,
                        "freqCaseView", binding.frequencyMenu))

        queue.show()
    }

    private fun getFreq(item: String): String {
        return when (item) {
            "Weekly" -> "weekly"
            "Monthly" -> "monthly"
            "Quarterly" -> "quarterly"
            "Half-yearly" -> "half-year"
            "Yearly" -> "yearly"
            else -> ""
        }
    }

    override fun setWidgets(){
        val spinnerAdapter = ArrayAdapter(requireContext(),
                R.layout.cat_exposed_dropdown_popup_item, resources.getStringArray(R.array.repeat_frequency))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.frequencyExposedDropdown.setAdapter(spinnerAdapter)
        binding.frequencyExposedDropdown.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            repeatFreq = when (position) {
                0 -> "weekly"
                1 -> "monthly"
                2 -> "quarterly"
                3 -> "half-year"
                4 -> "yearly"
                else -> ""
            }
        }
        binding.billDateEdittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(childFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                binding.billDateEdittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        binding.currencyEdittext.setOnClickListener{
            CurrencyListBottomSheet().show(childFragmentManager, "currencyList" )
        }
        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currencyCode ->
            currency = currencyCode
        }

        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) {
            binding.currencyEdittext.setText(it)
        }
        binding.placeHolderToolbar.setNavigationOnClickListener{ handleBack() }
        billViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
            val currencyData = defaultCurrency.currencyAttributes
            binding.currencyEdittext.setText(currencyData.name + " (" + currencyData.code + ")")
            currency = currencyData.code
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            binding.notesEdittext.setText(markdownText)
        }
        binding.notesEdittext.setOnClickListener {
            markdownViewModel.markdownText.postValue(binding.notesEdittext.getString())
            parentFragmentManager.commit {
                add(R.id.bigger_fragment_container, MarkdownFragment())
                addToBackStack(null)
            }
        }
        billViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        billViewModel.apiResponse.observe(viewLifecycleOwner){ response ->
            toastInfo(response, Toast.LENGTH_LONG)
        }
        binding.addAttachmentButton.setOnClickListener {
            attachmentDialog()
        }
    }

    private fun attachmentDialog(){
        val listItems = arrayOf(getString(R.string.capture_image_from_camera), getString(R.string.choose_file))
        AlertDialog.Builder(requireContext())
                .setItems(listItems) { dialog, which ->
                    when (which) {
                        0 -> {
                            val createTempDir = File(requireContext().getExternalFilesDir(null).toString() +
                                    File.separator + "temp")
                            if(!createTempDir.exists()){
                                createTempDir.mkdir()
                            }
                            val randomId = UUID.randomUUID().toString().substring(0, 7)
                            val fileToOpen = File(requireContext().getExternalFilesDir(null).toString() +
                                    File.separator + "temp" + File.separator + "${randomId}-firefly.png")
                            if(fileToOpen.exists()){
                                fileToOpen.delete()
                            }
                            fileUri = FileProvider.getUriForFile(requireContext(),
                                    requireContext().packageName + ".provider", fileToOpen)
                            takePicture.launch(fileUri)
                        }
                        1 -> {
                            chooseDocument.launch(arrayOf("*/*"))
                        }
                    }
                }
                .show()
    }


    override fun submitData(){
        billViewModel.addBill(binding.descriptionEdittext.getString(),
                binding.minAmountEdittext.getString(), binding.maxAmountEdittext.getString(),
                binding.billDateEdittext.getString(), repeatFreq, binding.skipEdittext.getString(), "1",
                    currency, notes, attachmentItemAdapter).observe(viewLifecycleOwner) { response ->
            if(response.first){
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                toastSuccess(response.second)
                handleBack()
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                toastInfo(response.second)
            }
        }
    }

    private fun updateBill(){
        billViewModel.updateBill(billId, binding.descriptionEdittext.getString(),
                binding.minAmountEdittext.getString(), binding.maxAmountEdittext.getString(),
                binding.billDateEdittext.getString(), getFreq(binding.frequencyExposedDropdown.getString()),
                binding.skipEdittext.getString(), "1", currency, notes).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun handleBack() {
        unReveal(binding.dialogAddBillLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAddBillBinding = null
        markdownViewModel.markdownText.postValue("")
    }
}