package xyz.hisname.fireflyiii.ui.account

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import kotlinx.android.synthetic.main.fragment_add_account.*
import me.toptas.fancyshowcase.FancyShowCaseQueue
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.MarkdownViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyBottomSheetViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File
import java.util.*

class AddAccountFragment: BaseAddObjectFragment() {

    private val accountType: String by lazy { arguments?.getString("accountType") ?: "" }
    private val accountId: Long by lazy { arguments?.getLong("accountId") ?: 0L }
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val accountViewModel by lazy { getImprovedViewModel(AddAccountViewModel::class.java)}
    private lateinit var fileUri: Uri
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private val attachmentDataAdapter by lazy { arrayListOf<AttachmentData>() }
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_account, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(add_account_layout)
        updateData()
        placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        if(accountId != 0L){
            addAccountFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        addAccountFab.setOnClickListener {
            submitData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", "", FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0))
                attachmentItemAdapter.add(fileUri)
                attachment_information.adapter?.notifyDataSetChanged()
            }
        }
        chooseDocument = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){ fileChoosen ->
            if(fileChoosen != null){
                fileChoosen.forEach { file ->
                    attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                            "", "", FileUtils.getFileName(requireContext(), file) ?: "",
                            "", "", "", 0, "", "", ""), 0))
                }
                attachmentItemAdapter.addAll(fileChoosen)
                attachment_information.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun setIcons() {
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        start_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_hourglass_start
                    sizeDp = 24
                    colorRes = R.color.md_red_400
                },null, null, null)
        start_date_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_calendar
                    colorRes = R.color.md_blue_400
                    sizeDp = 24
                },null, null, null)
        interest_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_percent
                    colorRes = R.color.md_amber_700
                    sizeDp = 24
                },null, null, null)
        iban_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_list_numbered
                    colorRes = R.color.md_deep_orange_900
                    sizeDp = 24
                },null, null, null)
        bic_edittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = GoogleMaterial.Icon.gmd_transfer_within_a_station
                                colorRes = R.color.md_deep_orange_400
                                sizeDp = 24
                            },null, null, null)
        account_number_edittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = GoogleMaterial.Icon.gmd_confirmation_number
                                colorRes = R.color.md_brown_600
                                sizeDp = 24
                            },null, null, null)
        opening_balance_edittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = GoogleMaterial.Icon.gmd_open_with
                                colorRes = R.color.md_red_A100
                                sizeDp = 24
                            },null, null, null)
        opening_balance_date_edittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = FontAwesome.Icon.faw_calendar
                                colorRes = R.color.md_blue_400
                                sizeDp = 24
                            },null, null, null)
        virtual_balance_edittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = FontAwesome.Icon.faw_balance_scale
                                colorRes = R.color.md_light_blue_A200
                                sizeDp = 24
                            },null, null, null)
        addAccountFab.setBackgroundColor(getCompatColor(R.color.colorPrimaryDark))
        addAccountFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
        add_attachment_button.setOnClickListener {
            attachmentDialog()
        }
        attachment_information.layoutManager = LinearLayoutManager(requireContext())
        attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        attachment_information.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            attachment_information.adapter?.notifyDataSetChanged()
        }) { another: Int -> }
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

    private fun setAccordion(){
        expansionLayout.addListener { _, expanded ->
            if(expanded){
                showHiddenHelpText()
            }
        }
    }

    override fun setWidgets() {
        setAccordion()
        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currency ->
            accountViewModel.currency = currency
        }
        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) {
            currency_edittext.setText(it)
        }
        currency_edittext.setOnClickListener{
            val currencyListFragment = CurrencyListBottomSheet()
            currencyListFragment.show(childFragmentManager, "currencyList" )
        }
        if(accountId == 0L) {
            accountViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
                val currencyData = defaultCurrency.currencyAttributes
                currency_edittext.setText(currencyData.name + " (" + currencyData.code + ")")
            }
        }
        if(accountType == "asset"){
            opening_balance_date_layout.isVisible = true
            opening_balance_date_edittext.setOnClickListener {
                val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                val picker = materialDatePicker.build()
                picker.show(childFragmentManager, picker.toString())
                picker.addOnPositiveButtonClickListener { time ->
                    opening_balance_date_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
                }
            }
            virtual_balance_layout.isVisible = true
            opening_balance_layout.isVisible = true
            accountRoleSpinner.isVisible = true
            currency_layout.isVisible = true
        }
        if(accountType == "liabilities"){
            currency_layout.isVisible = true
            liabilityTypeSpinner.isVisible = true
            start_amount_layout.isVisible = true
            start_date_layout.isVisible = true
            start_amount_text.isVisible = true
            start_date_edittext.setOnClickListener {
                val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                val picker = materialDatePicker.build()
                picker.show(childFragmentManager, picker.toString())
                picker.addOnPositiveButtonClickListener { time ->
                    start_date_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
                }
            }
            interest_layout.isVisible = true
            interestPeriodSpinner.isVisible = true
        }
        includeInNetWorthText.setOnClickListener {
            includeInNetWorthCheck.performClick()
        }
        accountViewModel.isLoading.observe(viewLifecycleOwner) { loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        note_edittext.setOnClickListener {
            markdownViewModel.markdownText.postValue(note_edittext.getString())
            parentFragmentManager.commit {
                add(R.id.bigger_fragment_container, MarkdownFragment())
                addToBackStack(null)
            }
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            note_edittext.setText(markdownText)
        }
    }

    override fun submitData() {
        val netWorth = includeInNetWorthCheck.isChecked
        val accountRoleSpinner: String? = if(accountRoleSpinner.isVisible){
            when {
                accountRoleSpinner.selectedItemPosition == 0 -> "defaultAsset"
                accountRoleSpinner.selectedItemPosition == 1 -> "sharedAsset"
                accountRoleSpinner.selectedItemPosition == 2 -> "savingAsset"
                accountRoleSpinner.selectedItemPosition == 3 -> "ccAsset"
                else -> "cashWalletAsset "
            }
        } else {
            null
        }
        val iBanString = if (iban_edittext.isBlank()) {
            null
        } else {
            iban_edittext.getString()
        }
        val bicString = if (bic_edittext.isBlank()) {
            null
        } else {
            bic_edittext.getString()
        }
        val accountNumberString = if (account_number_edittext.isBlank()) {
            null
        } else {
            account_number_edittext.getString()
        }
        val openingBalanceValue: String? = if(opening_balance_layout.isVisible){
            opening_balance_edittext.getString()
        } else {
            null
        }
        val openingBalanceDate: String? = if(opening_balance_date_layout.isVisible){
            opening_balance_date_edittext.getString()
        } else {
            null
        }
        val virtualBalanceString: String? = if(virtual_balance_layout.isVisible){
            virtual_balance_edittext.getString()
        } else {
            null
        }
        val notesText = if (note_edittext.isBlank()) {
            null
        } else {
            note_edittext.getString()
        }
        val currencyToBeSubmitted = if(currency_layout.isVisible && accountViewModel.currency.isNotBlank()){
            accountViewModel.currency
        } else {
            null
        }
        val liabilityType: String? = if(liabilityTypeSpinner.isVisible){
            when {
                liabilityTypeSpinner.selectedItemPosition == 0 -> "debt"
                liabilityTypeSpinner.selectedItemPosition == 1 -> "loan"
                else -> "mortgage"
            }
        } else {
            null
        }
        val startAmountOfDebt: String? = if(start_amount_layout.isVisible){
            start_amount_edittext.getString()
        } else {
            null
        }
        val startAmountDay: String? = if(start_date_layout.isVisible){
            start_date_edittext.getString()
        } else {
            null
        }
        val interestAmount: String? = if(interest_layout.isVisible){
            interest_edittext.getString()
        } else {
            null
        }
        val interestPeriod: String? = if(interestPeriodSpinner.isVisible){
            when {
                interestPeriodSpinner.selectedItemPosition == 0 -> "daily"
                interestPeriodSpinner.selectedItemPosition == 1 -> "monthly"
                else -> "yearly"
            }
        } else {
            null
        }
        if(accountId == 0L){
            addAccount(description_edittext.getString(), accountType, currencyToBeSubmitted,
                    iBanString, bicString, accountNumberString, openingBalanceValue, openingBalanceDate,
                    accountRoleSpinner, virtualBalanceString, netWorth, notesText, liabilityType, startAmountOfDebt,
                    startAmountDay, interestAmount, interestPeriod)
        } else {
            updateAccount(description_edittext.getString(), accountType, currencyToBeSubmitted,
                    iBanString, bicString, accountNumberString, openingBalanceValue, openingBalanceDate,
                    accountRoleSpinner, virtualBalanceString, netWorth, notesText, liabilityType, startAmountOfDebt,
                    startAmountDay, interestAmount, interestPeriod)
        }
    }

    private fun updateAccount(accountName: String, accountType: String,
                              currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                              openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                              virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                              liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?){
        accountViewModel.updateAccount(accountId,accountName, accountType, currencyCode,
                iban, bic, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod).observe(viewLifecycleOwner){ response ->
            if(response.first){
                toastSuccess("Account saved")
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun addAccount(accountName: String, accountType: String,
                           currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                           openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                           virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                           liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?){
        accountViewModel.addAccount(accountName, accountType, currencyCode,
                iban, bic, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod, attachmentItemAdapter).observe(viewLifecycleOwner){ response ->
            if(response.first){
                handleBack()
                toastSuccess(response.second)
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun updateData(){
        if(accountId != 0L){
            accountViewModel.getAccountById(accountId).observe(viewLifecycleOwner) { accountData ->
                val accountAttributes = accountData.accountAttributes
                description_edittext.setText(accountAttributes.name)
                currency_edittext.setText(accountAttributes.currency_code + " (" + accountAttributes.currency_symbol + " )")
                accountViewModel.currency = accountAttributes.currency_code ?: ""
                val liabilityType = accountAttributes.liability_type
                if(liabilityType != null){
                    when (liabilityType) {
                        "debt" -> liabilityTypeSpinner.setSelection(0)
                        "loan" -> liabilityTypeSpinner.setSelection(1)
                        "mortgage" -> liabilityTypeSpinner.setSelection(2)
                    }
                }
                start_amount_edittext.setText(accountAttributes.liability_amount)
                start_date_edittext.setText(accountAttributes.liability_start_date)
                interest_edittext.setText(accountAttributes.interest)
                val interestPeriod = accountAttributes.interest_period
                if(interestPeriod != null){
                    when(interestPeriod){
                        "daily" -> interestPeriodSpinner.setSelection(0)
                        "monthly" -> interestPeriodSpinner.setSelection(1)
                        "yearly" -> interestPeriodSpinner.setSelection(2)
                    }
                }
                iban_edittext.setText(accountAttributes.iban)
                bic_edittext.setText(accountAttributes.bic)
                account_number_edittext.setText(accountAttributes.account_number)
                if(accountAttributes.include_net_worth){
                    includeInNetWorthCheck.performClick()
                }
                opening_balance_edittext.setText(accountAttributes.opening_balance.toString())
                opening_balance_date_edittext.setText(accountAttributes.opening_balance_date)
                val accountRole = accountAttributes.account_role
                if(accountRole != null){
                    when(accountRole){
                        "defaultAsset" -> accountRoleSpinner.setSelection(0)
                        "sharedAsset" -> accountRoleSpinner.setSelection(1)
                        "savingAsset" -> accountRoleSpinner.setSelection(2)
                        "ccAsset" -> accountRoleSpinner.setSelection(3)
                        "cashWalletAsset" -> accountRoleSpinner.setSelection(4)
                    }
                }
                virtual_balance_edittext.setText(accountAttributes.virtual_balance.toString())
                note_edittext.setText(accountAttributes.notes)
            }
        } else {
            showHelpText()
        }
    }

    private fun showHelpText(){
        FancyShowCaseQueue()
                .add(showCase(R.string.add_account_currency_help_text,
                        "addAccountCurrencyCaseView", currency_layout)).show()
    }

    private fun showHiddenHelpText(){
        if(iban_layout.isVisible && expansionLayout.isExpanded) {
            val queue = FancyShowCaseQueue()
            queue.add(showCase(R.string.iban_help_text, "ibanCaseView", iban_layout, true))
            if(opening_balance_layout.isVisible){
                queue.add(showCase(R.string.opening_balance_help_text, "openingBalanceCaseView", opening_balance_layout))
            }
            if(virtual_balance_layout.isVisible) {
                queue.add(showCase(R.string.virtual_balance_help_text, "virtualBalanceCaseView",
                        virtual_balance_layout))
            }
            queue.show()
        }
    }
    
    override fun handleBack() {
        if(accountId == 0L) {
            unReveal(add_account_layout, true)
        } else {
            unReveal(add_account_layout)
        }
        markdownViewModel.markdownText.postValue("")
    }

}