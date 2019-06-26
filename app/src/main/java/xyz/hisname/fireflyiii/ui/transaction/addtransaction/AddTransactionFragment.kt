package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hootsuite.nachos.ChipConfiguration
import com.hootsuite.nachos.chip.ChipCreator
import com.hootsuite.nachos.chip.ChipSpan
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.receiver.TransactionReceiver
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.AddAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.budget.BudgetSearchDialog
import xyz.hisname.fireflyiii.ui.categories.CategoriesDialog
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.ui.piggybank.PiggyDialog
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.DialogDarkMode
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.workers.transaction.AttachmentWorker
import java.util.*

class AddTransactionFragment: BaseFragment() {

    private val transactionType by lazy { arguments?.getString("transactionType") ?: "" }
    private val nastyHack by lazy { arguments?.getBoolean("SHOULD_HIDE") ?: false }
    private val transactionId by lazy { arguments?.getLong("transactionId") ?: 0 }
    private var currency = ""
    private var tags = ArrayList<String>()
    private var piggyBankList = ArrayList<String>()
    private var piggyBank: String? = ""
    private var categoryName: String? = ""
    private var transactionTags: String? = ""
    private var sourceAccount = ""
    private var destinationAccount = ""
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private var sourceName: String? = ""
    private var destinationName: String? = ""
    private val calendar by lazy {  Calendar.getInstance() }
    private var selectedTime = ""
    private var budgetName: String? = ""

    companion object {
        private const val OPEN_REQUEST_CODE  = 41
        private const val STORAGE_REQUEST_CODE = 1337
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setIcons()
        setWidgets()
        if(transactionId != 0L){
            add_attachment_button.isVisible = true
            add_attachment_button.setOnClickListener {
                openDocViewer()
            }
            updateTransactionSetup()
        }
        contextSwitch()
        setFab()
    }

    private fun setFab(){
        addTransactionFab.setOnClickListener {
            ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            hideKeyboard()
            piggyBank = if(piggy_edittext.isBlank()){
                null
            } else {
                piggy_edittext.getString()
            }
            categoryName = if(category_edittext.isBlank()){
                null
            } else {
                category_edittext.getString()
            }
            transactionTags = if(tags_chip.allChips.isNullOrEmpty()){
                null
            } else {
                // Remove [ and ] from beginning and end of string
                val beforeTags = tags_chip.allChips.toString().substring(1)
                beforeTags.substring(0, beforeTags.length - 1)
            }
            budgetName = if(budget_edittext.isBlank()){
                null
            } else {
                budget_edittext.getString()
            }
            when {
                Objects.equals("Withdrawal", transactionType) -> {
                    sourceAccount = source_exposed_dropdown.getString()
                    destinationAccount = destination_edittext.getString()
                }
                Objects.equals("Transfer", transactionType) -> {
                    sourceAccount = source_exposed_dropdown.getString()
                    destinationAccount = destination_exposed_dropdown.getString()
                }
                Objects.equals("Deposit", transactionType) -> {
                    sourceAccount = source_edittext.getString()
                    destinationAccount = destination_exposed_dropdown.getString()
                }
            }
            if(transactionId != 0L){
                updateData()
            } else {
                submitData()
            }
        }
    }

    private fun updateData(){
        val transactionDateTime = if (time_layout.isVisible && selectedTime.isNotBlank()){
            transaction_date_edittext.getString() + " " + selectedTime
        } else {
            transaction_date_edittext.getString()
        }
        transactionViewModel.updateTransaction(transactionId,transactionType, description_edittext.getString(),
                transactionDateTime, transaction_amount_edittext.getString(),
                sourceAccount, destinationAccount, currency, categoryName,
                transactionTags, budgetName).observe(this) { transactionResponse->
            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            val errorMessage = transactionResponse.getErrorMessage()
            if (transactionResponse.getResponse() != null) {
                toastSuccess(resources.getString(R.string.transaction_updated))
                handleBack()
            } else if(errorMessage != null) {
                toastError(errorMessage)
            } else if(transactionResponse.getError() != null) {
                toastError(transactionResponse.getError()?.localizedMessage)
            }
        }
    }

    private fun setIcons(){
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                        .color(getCompatColor(R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        transaction_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_dollar_sign)
                        .color(getCompatColor(R.color.md_yellow_A700))
                        .sizeDp(16),null, null, null)
        transaction_date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_calendar)
                .color(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
                .sizeDp(24),null, null, null)
        source_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_exchange_alt).sizeDp(24),null, null, null)
        destination_edittext.setCompoundDrawablesWithIntrinsicBounds(
                getCompatDrawable(R.drawable.ic_bank_transfer),null, null, null)
        category_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_chart_bar)
                        .color(getCompatColor(R.color.md_deep_purple_400))
                        .sizeDp(24),null, null, null)
        piggy_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_piggy_bank)
                        .color(getCompatColor(R.color.md_pink_200))
                        .sizeDp(24),null, null, null)
        time_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_clock)
                        .color(getCompatColor(R.color.md_red_400))
                        .sizeDp(24),null, null, null)
        tags_chip.chipTokenizer = SpanChipTokenizer(requireContext(), object : ChipCreator<ChipSpan> {
            override fun configureChip(chip: ChipSpan, chipConfiguration: ChipConfiguration) {
            }

            override fun createChip(context: Context, text: CharSequence, data: Any?): ChipSpan {
                return ChipSpan(requireContext(), text,
                        IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_tag).sizeDp(12), data)
            }

            override fun createChip(context: Context, existingChip: ChipSpan): ChipSpan {
                return ChipSpan(requireContext(), existingChip)
            }
        }, ChipSpan::class.java)
        addTransactionFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_plus)
                .color(getCompatColor(R.color.md_black_1000))
                .sizeDp(24))
        budget_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_gratipay)
                        .color(getCompatColor(R.color.md_amber_300))
                        .sizeDp(24),null, null, null)
    }

    private fun setWidgets(){
        transaction_date_edittext.setText(DateTimeUtil.getTodayDate())
        val transactionDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                transaction_date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }
        transaction_date_edittext.setOnClickListener {
            DialogDarkMode().showCorrectDatePickerDialog(requireContext(), transactionDate, calendar)
        }
        category_edittext.setOnClickListener {
            val catDialog = CategoriesDialog()
            catDialog.show(requireFragmentManager(), "categoryDialog")
        }
        categoryViewModel.categoryName.observe(this) {
            category_edittext.setText(it)
        }
        currencyViewModel.currencyCode.observe(this) {
            currency = it
        }
        currencyViewModel.currencyDetails.observe(this) {
            currency_edittext.setText(it)
        }
        currency_edittext.setOnClickListener{
            CurrencyListBottomSheet().show(requireFragmentManager(), "currencyList" )
        }
        tags_chip.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR)
        tags_chip.enableEditChipOnTouch(false, true)
        tagsViewModel.getAllTags().observe(this) {
            it.forEachIndexed{ _, tagsData ->
                tags.add(tagsData.tagsAttributes?.tag!!)
            }
            val tagsAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, tags)
            tags_chip.threshold = 1
            tags_chip.setAdapter(tagsAdapter)
        }
        currencyViewModel.getDefaultCurrency().observe(this) { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            currency = currencyData?.code.toString()
            currency_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
        }
        accountViewModel.isLoading.observe(this) {
            if(it == true){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        accountViewModel.emptyAccount.observe(this) {
            if(it == true){
                AlertDialog.Builder(requireContext())
                        .setTitle("No asset accounts found!")
                        .setMessage("We tried searching for an asset account but is unable to find any. Would you like" +
                                "to add an asset account first? ")
                        .setPositiveButton("OK"){ _,_ ->
                            requireFragmentManager().commit {
                                replace(R.id.bigger_fragment_container, AddAccountFragment())
                                arguments = bundleOf("accountType" to "asset")
                            }
                        }
                        .setNegativeButton("No"){ _,_ ->
                            handleBack()
                        }
                        .setCancelable(false)
                        .show()
            }
        }
        piggy_edittext.setOnClickListener {
            val piggyBankDialog = PiggyDialog()
            piggyBankDialog.show(requireFragmentManager(), "piggyDialog")
        }
        piggyViewModel.piggyName.observe(this) {
            piggy_edittext.setText(it)
        }
        budget_edittext.setOnClickListener {
            val budgetDialog = BudgetSearchDialog()
            budgetDialog.show(requireFragmentManager(), "budgetDialog")
        }
        budgetViewModel.budgetName.observe(this) { name ->
            budget_edittext.setText(name)
        }
        expansionLayout.addListener { _, expanded ->
            if(expanded){
                optionalLayout.isVisible = true
                if (piggy_layout.isVisible){
                    showCase(R.string.transactions_create_transfer_ffInput_piggy_bank_id,
                            "transactionPiggyShowCase", piggy_layout).show()
                }
            } else {
                optionalLayout.isInvisible = true
            }
        }
        placeHolderToolbar.navigationIcon = getCompatDrawable(R.drawable.abc_ic_clear_material)
        placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        time_edittext.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(requireContext(), TimePickerDialog.OnTimeSetListener {
                _, selectedHour, selectedMin ->
                // We don't have to be really accurate down to seconds.
                selectedTime = "$selectedHour:$selectedMin"
                time_edittext.setText(selectedTime)
            },hour, minute, true).show()
        }
    }

    private fun contextSwitch(){
        when {
            Objects.equals(transactionType, "Transfer") -> accountViewModel.getAccountNameByType("asset")
                    .observe(this) { transferData ->
                        source_exposed_menu.isVisible = true
                        source_layout.isGone = true
                        destination_layout.isGone = true
                        destination_exposed_menu.isVisible = true
                        piggy_layout.isVisible = true
                        spinnerAdapter = ArrayAdapter(requireContext(),
                                R.layout.cat_exposed_dropdown_popup_item, transferData)
                        val destinationPosition = spinnerAdapter.getPosition(destinationName)
                        destination_exposed_dropdown.setSelection(destinationPosition)
                        source_exposed_dropdown.setAdapter(spinnerAdapter)
                    }
            Objects.equals(transactionType, "Deposit") -> zipLiveData(accountViewModel.getAccountNameByType("revenue"),
                    accountViewModel.getAccountNameByType("asset")).observe(this ) {
                // Asset account, spinner
                spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it.second)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                destination_exposed_dropdown.setAdapter(spinnerAdapter)
                destination_layout.isVisible = false
                // Revenue account, autocomplete
                val autocompleteAdapter = ArrayAdapter(requireContext(),
                        R.layout.cat_exposed_dropdown_popup_item, it.first)
                source_edittext.threshold = 1
                source_edittext.setAdapter(autocompleteAdapter)
                source_exposed_menu.isVisible = false
            }
            else -> {
                zipLiveData(accountViewModel.getAccountNameByType("asset"),
                        accountViewModel.getAccountNameByType("expense")).observe(this) { accountNames ->
                    source_layout.isVisible = false
                    destination_exposed_menu.isVisible = false
                    // Spinner for source account
                    spinnerAdapter = ArrayAdapter(requireContext(),
                            R.layout.cat_exposed_dropdown_popup_item, accountNames.first)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    source_exposed_dropdown.setAdapter(spinnerAdapter)
                    // This is used for auto complete for destination account
                    val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, accountNames.second)
                    destination_edittext.threshold = 1
                    destination_edittext.setAdapter(autocompleteAdapter)
                }
            }
        }
    }

    private fun submitData() {
        val transactionDateTime = if (time_layout.isVisible && selectedTime.isNotBlank()){
            transaction_date_edittext.getString() + " " + selectedTime
        } else {
            transaction_date_edittext.getString()
        }
        transactionViewModel.addTransaction(transactionType, description_edittext.getString(),
                transactionDateTime, piggyBank, transaction_amount_edittext.getString(),
                sourceAccount, destinationAccount,
                currency, categoryName, transactionTags, budgetName).observe(this) { transactionResponse ->
            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            val errorMessage = transactionResponse.getErrorMessage()
            if (transactionResponse.getResponse() != null) {
                toastSuccess(resources.getString(R.string.transaction_added))
                handleBack()
            } else if (errorMessage != null) {
                toastError(errorMessage)
            } else if (transactionResponse.getError() != null) {
                when {
                    Objects.equals("transfers", transactionType) -> {
                        val transferBroadcast = Intent(requireContext(), TransactionReceiver::class.java).apply {
                            action = "firefly.hisname.ADD_TRANSFER"
                        }
                        val extras = bundleOf(
                                "description" to description_edittext.getString(),
                                "date" to transactionDateTime,
                                "amount" to transaction_amount_edittext.getString(),
                                "currency" to currency,
                                "sourceName" to sourceAccount,
                                "destinationName" to destinationAccount,
                                "piggyBankName" to piggyBankList,
                                "category" to categoryName,
                                "tags" to transactionTags
                        )
                        transferBroadcast.putExtras(extras)
                        requireActivity().sendBroadcast(transferBroadcast)
                    }
                    Objects.equals("Deposit", transactionType) -> {
                        val transferBroadcast = Intent(requireContext(), TransactionReceiver::class.java).apply {
                            action = "firefly.hisname.ADD_DEPOSIT"
                        }
                        val extras = bundleOf(
                                "description" to description_edittext.getString(),
                                "date" to transactionDateTime,
                                "amount" to transaction_amount_edittext.getString(),
                                "currency" to currency,
                                "destinationName" to destinationAccount,
                                "category" to categoryName,
                                "tags" to transactionTags
                        )
                        transferBroadcast.putExtras(extras)
                        requireActivity().sendBroadcast(transferBroadcast)
                    }
                    Objects.equals("Withdrawal", transactionType) -> {
                        val withdrawalBroadcast = Intent(requireContext(), TransactionReceiver::class.java).apply {
                            action = "firefly.hisname.ADD_WITHDRAW"
                        }
                        val extras = bundleOf(
                                "description" to description_edittext.getString(),
                                "date" to transactionDateTime,
                                "amount" to transaction_amount_edittext.getString(),
                                "currency" to currency,
                                "sourceName" to sourceAccount,
                                "category" to categoryName,
                                "tags" to transactionTags
                        )
                        withdrawalBroadcast.putExtras(extras)
                        requireActivity().sendBroadcast(withdrawalBroadcast)
                    }
                }
                toastOffline(getString(R.string.data_added_when_user_online, transactionType))
                handleBack()
            }
        }
    }

    private fun updateTransactionSetup(){
        transactionViewModel.getTransactionById(transactionId).observe(this) {
            val transactionAttributes = it[0].transactionAttributes
            description_edittext.setText(transactionAttributes?.description)
            transaction_amount_edittext.setText(Math.abs(transactionAttributes?.amount
                    ?: 0.toDouble()).toString())
            budget_edittext.setText(transactionAttributes?.budget_name)
            currencyViewModel.getCurrencyByCode(transactionAttributes?.currency_code.toString()).observe(this) { currencyData ->
                val currencyAttributes = currencyData[0].currencyAttributes
                currency_edittext.setText(currencyAttributes?.name + " (" + currencyAttributes?.code + ")")
            }
            currency = transactionAttributes?.currency_code.toString()
            transaction_date_edittext.setText(transactionAttributes?.date.toString())
            piggy_edittext.setText(transactionAttributes?.piggy_bank_name)
            category_edittext.setText(transactionAttributes?.category_name)
            if(transactionAttributes?.tags != null){
                tags_chip.setText(transactionAttributes.tags + ",")
            }
            when {
                Objects.equals("Withdrawal", transactionType) -> {
                    destination_edittext.setText(transactionAttributes?.destination_name)
                    sourceName = transactionAttributes?.source_name
                }
                Objects.equals("Transfer", transactionType) -> {
                    sourceName = transactionAttributes?.source_name
                    destinationName = transactionAttributes?.destination_name
                }
                Objects.equals("Deposit", transactionType) -> {
                    source_edittext.setText(transactionAttributes?.source_name)
                    destinationName = transactionAttributes?.destination_name
                }
            }
        }
    }

    private fun openDocViewer(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else {
            val documentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(documentIntent, OPEN_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.size == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openDocViewer()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if(resultCode == Activity.RESULT_OK){
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    val fileUri = resultData.data
                    AttachmentWorker.initWorker(fileUri, transactionId)
                    toastInfo("File will be uploaded in the background")
                }
            }
        }
    }

    override fun handleBack() {
        if(nastyHack){
            requireFragmentManager().popBackStack()
            fragmentContainer.isVisible = true
            fragment_add_transaction_root.isVisible = false
            requireActivity().findViewById<FloatingActionButton>(R.id.addTransactionFab).isVisible = true
        } else {
            requireActivity().finish()
        }
    }
}