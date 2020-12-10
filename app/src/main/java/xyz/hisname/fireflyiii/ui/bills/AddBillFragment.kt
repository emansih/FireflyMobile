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
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_add_bill.*
import kotlinx.android.synthetic.main.fragment_add_bill.add_attachment_button
import kotlinx.android.synthetic.main.fragment_add_bill.appbar
import kotlinx.android.synthetic.main.fragment_add_bill.attachment_information
import kotlinx.android.synthetic.main.fragment_add_bill.currency_edittext
import kotlinx.android.synthetic.main.fragment_add_bill.description_edittext
import kotlinx.android.synthetic.main.fragment_add_bill.placeHolderToolbar
import me.toptas.fancyshowcase.FancyShowCaseQueue
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.MarkdownViewModel
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
    private val attachmentDataAdapter by lazy { arrayListOf<AttachmentData>() }
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_bill, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_bill_layout)
        updateEditText()
        showHelpText()
        setFab()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachment_information.isVisible = true
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", "", FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0, ""))
                attachmentItemAdapter.add(fileUri)
                attachment_information.adapter?.notifyDataSetChanged()
            }
        }
        chooseDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()){ fileChoosen ->
            attachment_information.isVisible = true
            if(fileChoosen != null){
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", "", FileUtils.getFileName(requireContext(), fileChoosen) ?: "",
                        "", "", "", 0, "", "", ""), 0, ""))
                attachmentItemAdapter.add(fileChoosen)
                attachment_information.adapter?.notifyDataSetChanged()
            }
        }
    }


    private fun updateEditText(){
        if(billId != 0L){
            billViewModel.getBillById(billId).observe(viewLifecycleOwner) { billData ->
                billAttribute = billData.billAttributes
                description_edittext.setText(billAttribute?.name)
                billDescription = billAttribute?.name
                min_amount_edittext.setText(billAttribute?.amount_min.toString())
                max_amount_edittext.setText(billAttribute?.amount_max.toString())
                currency = billAttribute?.currency_code ?: ""
                billViewModel.getBillCurrencyDetails(billId).observe(viewLifecycleOwner){ currencyDetails ->
                    currency_edittext.setText(currencyDetails)
                }
                bill_date_edittext.setText(billAttribute?.date.toString())
                skip_edittext.setText(billAttribute?.skip.toString())
                notes_edittext.setText(billAttribute?.notes)
                frequency_exposed_dropdown.setText(billAttribute?.repeat_freq?.substring(0, 1)?.toUpperCase()
                        + billAttribute?.repeat_freq?.substring(1))

                // Weird bug where only 1 value will show in the array if I don't use this
                val spinnerAdapter = ArrayAdapter(requireContext(),
                        R.layout.cat_exposed_dropdown_popup_item, resources.getStringArray(R.array.repeat_frequency))
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                frequency_exposed_dropdown.setAdapter(spinnerAdapter)
            }
        }
    }

    private fun setFab(){
        if(billId != 0L){
            addBillFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        addBillFab.setOnClickListener {
            hideKeyboard()
            notes = if(notes_edittext.isBlank()){
                null
            } else {
                notes_edittext.getString()
            }
            if(billId == 0L){
                submitData()
            } else {
                updateBill()
            }
        }
    }

    override fun setIcons(){
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        min_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_dollar_sign
                    colorRes = R.color.md_yellow_A700
                    sizeDp = 16
                },null, null, null)
        max_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_dollar_sign
                    colorRes = R.color.md_yellow_A700
                    sizeDp = 16
                },null, null, null)
        bill_date_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_calendar
                    color = colorList(ColorStateList.valueOf(rgb(18, 122, 190)))
                    sizeDp = 24
                },null, null, null)
        skip_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_sort_numeric_up
                    colorRes = R.color.md_red_500
                    sizeDp = 24
                },null, null, null)
        addBillFab.setBackgroundColor(getCompatColor(R.color.colorPrimaryDark))
        addBillFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
        attachment_information.layoutManager = LinearLayoutManager(requireContext())
        attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        attachment_information.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            attachment_information.adapter?.notifyDataSetChanged()
        }) { another: Int -> }

    }

    private fun showHelpText(){
        queue = FancyShowCaseQueue()
                .add(showCase(R.string.bills_create_intro, "addBillDescriptionCaseView", appbar))
                .add(showCase(R.string.bills_create_name, "descriptionCaseView", description_edittext))
                .add(showCase(R.string.bills_create_amount_min_holder, "minMaxAmountCaseView",
                        min_amount_layout))
                .add(showCase(R.string.bills_create_skip_holder, "skipCaseView", skip_layout))
                .add(showCase(R.string.bills_create_repeat_freq_holder,
                        "freqCaseView", frequency_menu))

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
        frequency_exposed_dropdown.setAdapter(spinnerAdapter)
        frequency_exposed_dropdown.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            repeatFreq = when (position) {
                0 -> "weekly"
                1 -> "monthly"
                2 -> "quarterly"
                3 -> "half-year"
                4 -> "yearly"
                else -> ""
            }
        }
        bill_date_edittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                bill_date_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        currency_edittext.setOnClickListener{
            CurrencyListBottomSheet().show(parentFragmentManager, "currencyList" )
        }
        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currencyCode ->
            currency = currencyCode
        }

        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) {
            currency_edittext.setText(it)
        }
        placeHolderToolbar.setNavigationOnClickListener{ handleBack() }
        billViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
            val currencyData = defaultCurrency.currencyAttributes
            currency_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
            currency = currencyData?.code.toString()
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            notes_edittext.setText(markdownText)
        }
        notes_edittext.setOnClickListener {
            markdownViewModel.markdownText.postValue(notes_edittext.getString())
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
        add_attachment_button.setOnClickListener {
            attachmentDialog()
        }
    }

    private fun attachmentDialog(){
        val listItems = arrayOf("Capture image from camera", "Choose File")
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
        billViewModel.addBill(description_edittext.getString(),
                min_amount_edittext.getString(), max_amount_edittext.getString(),
                bill_date_edittext.getString(), repeatFreq, skip_edittext.getString(), "1",
                    currency, notes, attachmentItemAdapter).observe(viewLifecycleOwner) { response ->
            toastSuccess(response.second)
            if(response.first){
                if(attachmentItemAdapter.isEmpty()) {
                    handleBack()
                } else {
                    billViewModel.attachmentMessageLiveData.observe(viewLifecycleOwner){ error ->
                        if(error.isNotEmpty()){
                            val errorMessage = ""
                            error.forEachIndexed { index, s ->
                                errorMessage.plus("$index. $s\n\n")
                            }
                            AlertDialog.Builder(requireContext())
                                    .setTitle("There was some issue uploading your attachments")
                                    .setMessage(errorMessage)
                                    .setPositiveButton(android.R.string.ok){ _, _ ->
                                        handleBack()
                                    }
                            toastError("Please try again later", Toast.LENGTH_LONG)
                        } else {
                            toastSuccess("File uploaded!")
                            handleBack()
                        }
                    }
                }
            }
        }
    }

    private fun updateBill(){
        billViewModel.updateBill(billId, description_edittext.getString(),
                min_amount_edittext.getString(), max_amount_edittext.getString(),
                bill_date_edittext.getString(), getFreq(frequency_exposed_dropdown.getString()),
                skip_edittext.getString(), "1", currency, notes).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    override fun handleBack() {
        unReveal(dialog_add_bill_layout)
        markdownViewModel.markdownText.postValue("")
    }

}