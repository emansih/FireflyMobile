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

package xyz.hisname.fireflyiii.ui.piggybank

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentAddPiggyBinding
import xyz.hisname.fireflyiii.ui.markdown.MarkdownViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File
import java.util.*

class AddPiggyFragment: BaseAddObjectFragment() {

    private var currentAmount: String? = null
    private var startDate: String? = null
    private var targetDate: String? = null
    private var notes: String? = null
    private var groupTitle: String? = null
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val piggyViewModel by lazy { getImprovedViewModel(AddPiggyViewModel::class.java) }
    private val piggyId by lazy { arguments?.getLong("piggyId") ?: 0 }
    private lateinit var fileUri: Uri
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }
    private var fragmentAddPiggyBinding: FragmentAddPiggyBinding? = null
    private val binding get() = fragmentAddPiggyBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAddPiggyBinding = FragmentAddPiggyBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(binding.dialogAddPiggyLayout)
        updateEditText()
        setFab()
        showHelpText()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0))
                attachmentItemAdapter.add(fileUri)
                if (piggyId != 0L){
                    toastInfo("Uploading...")
                    piggyViewModel.uploadFile(piggyId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
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
                if (piggyId != 0L){
                    toastInfo("Uploading...")
                    piggyViewModel.uploadFile(piggyId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
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

    private fun attachmentDialog(){
        val listItems = arrayOf(getString(R.string.capture_image_from_camera), getString(R.string.choose_file))
        AlertDialog.Builder(requireContext())
                .setItems(listItems) { dialog, which ->
                    when (which) {
                        0 -> {
                            val createTempDir = File(requireContext().getExternalFilesDir(null).toString() +
                                    File.separator + "temp")
                            if (!createTempDir.exists()) {
                                createTempDir.mkdir()
                            }
                            val randomId = UUID.randomUUID().toString().substring(0, 7)
                            val fileToOpen = File(requireContext().getExternalFilesDir(null).toString() +
                                    File.separator + "temp" + File.separator + "${randomId}-firefly.png")
                            if (fileToOpen.exists()) {
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

    private fun showHelpText() = showCase(R.string.piggy_bank_description_help_text,
            "descriptionCaseView", binding.descriptionEdittext).show()

    private fun updateEditText(){
        if(piggyId != 0L){
            piggyViewModel.getPiggyById(piggyId).observe(viewLifecycleOwner) { piggyData ->
                val piggyAttributes = piggyData.piggyAttributes
                binding.descriptionEdittext.setText(piggyAttributes.name)
                binding.targetAmountEdittext.setText(piggyAttributes.target_amount.toString())
                binding.currentAmountEdittext.setText(piggyAttributes.current_amount.toString())
                binding.dateStartedEdittext.setText(piggyAttributes.start_date)
                binding.dateTargetEdittext.setText(piggyAttributes.target_date)
                binding.noteEdittext.setText(piggyAttributes.notes)
                piggyViewModel.getAccountById(piggyData.piggyAttributes.account_id ?: 0).observe(viewLifecycleOwner){ accountData ->
                    val accountAttributes = accountData.accountAttributes
                    binding.accountExposedDropdown.setText(accountAttributes.name + " (" +
                            accountAttributes.currency_symbol + accountAttributes.current_balance + ")")
                }
                displayAttachment()
            }
        }
    }

    private fun displayAttachment(){
        piggyViewModel.piggyAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        false, { data: AttachmentData ->
                    AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.are_you_sure))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                piggyViewModel.deleteAttachment(data).observe(viewLifecycleOwner) { isSuccessful ->
                                    if (isSuccessful) {
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

    private fun setAccordion(){
        binding.expansionLayout.addListener { _, expanded ->
            if(expanded){
                showCase(R.string.piggy_bank_date_help_text,
                        "dateStartedCaseView", binding.dateStartedEdittext).show()
            }
        }
    }

    private fun setFab(){
        if(piggyId != 0L){
            binding.addPiggyFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        binding.addPiggyFab.setOnClickListener {
            hideKeyboard()
            currentAmount = if (binding.currentAmountEdittext.isBlank()) {
                null
            } else {
                binding.currentAmountEdittext.getString()
            }
            startDate = if (binding.dateStartedEdittext.isBlank()) {
                null
            } else {
                binding.dateStartedEdittext.getString()
            }
            targetDate = if (binding.dateTargetEdittext.isBlank()) {
                null
            } else {
                binding.dateTargetEdittext.getString()
            }
            notes = if (binding.noteEdittext.isBlank()) {
                null
            } else {
                binding.noteEdittext.getString()
            }
            groupTitle = if(binding.groupEdittext.isBlank()){
                null
            } else {
                binding.groupEdittext.getString()
            }
            if(piggyId == 0L) {
                submitData()
            } else {
                updatePiggyBank()
            }
        }
    }

    override fun setIcons(){
        binding.targetAmountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                }, null, null, null)
        binding.currentAmountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                }, null, null, null)
        binding.dateStartedEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        }, null, null, null)
        binding.dateTargetEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        }, null, null, null)
        binding.addPiggyFab.setBackgroundColor(getCompatColor(R.color.colorPrimaryDark))
        binding.addPiggyFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
    }

    override fun setWidgets(){
        setAccordion()
        binding.dateTargetEdittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(childFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                binding.dateTargetEdittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        binding.dateStartedEdittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(childFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                binding.dateStartedEdittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        piggyViewModel.getAccount().observe(viewLifecycleOwner) { list ->
            val accountAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, list)
            accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.accountExposedDropdown.setAdapter(accountAdapter)
        }
        binding.accountExposedDropdown.setOnItemClickListener { parent, view, position, id ->
            piggyViewModel.getCurrentSelectedAccount(position)
        }
        binding.placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        binding.noteEdittext.setOnClickListener {
            markdownViewModel.markdownText.postValue(binding.noteEdittext.getString())
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, MarkdownFragment())
                addToBackStack(null)
            }
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            binding.noteEdittext.setText(markdownText)
        }
        piggyViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        binding.addAttachmentButton.setOnClickListener {
            attachmentDialog()
        }
        binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
        binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            binding.attachmentInformation.adapter?.notifyDataSetChanged()
        }) { another: Int -> }
        // https://github.com/firefly-iii/firefly-iii/issues/4435
        piggyViewModel.unSupportedVersion.observe(viewLifecycleOwner){ isNotSupported ->
            if(isNotSupported){
                binding.groupLayout.isGone = true
            } else {
                binding.groupLayout.isVisible = true
            }
        }
    }

    override fun submitData(){
        piggyViewModel.addPiggyBank(binding.descriptionEdittext.getString(), currentAmount, notes, startDate,
                binding.targetAmountEdittext.getString(), targetDate, groupTitle,
                attachmentItemAdapter).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }

    }

    private fun updatePiggyBank(){
        piggyViewModel.updatePiggyBank(piggyId, binding.descriptionEdittext.getString(),
                currentAmount, notes, startDate, binding.targetAmountEdittext.getString(),
                targetDate, groupTitle).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun handleBack() {
        unReveal(binding.dialogAddPiggyLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAddPiggyBinding = null
    }
}