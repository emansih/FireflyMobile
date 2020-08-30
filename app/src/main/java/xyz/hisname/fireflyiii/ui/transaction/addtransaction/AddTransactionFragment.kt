package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hootsuite.nachos.ChipConfiguration
import com.hootsuite.nachos.chip.ChipCreator
import com.hootsuite.nachos.chip.ChipSpan
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.receiver.TransactionReceiver
import xyz.hisname.fireflyiii.repository.budget.BudgetViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.AddAccountFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.budget.BudgetSearchDialog
import xyz.hisname.fireflyiii.ui.categories.CategoriesDialog
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.ui.piggybank.PiggyDialog
import xyz.hisname.fireflyiii.ui.tasker.TransactionPluginViewModel
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionAttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.TaskerPlugin
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*
import kotlin.math.abs

// This code sucks :(
class AddTransactionFragment: BaseFragment() {

    private lateinit var transactionType: String
    private val isTasker by lazy { arguments?.getBoolean("isTasker") ?: false }
    private val nastyHack by lazy { arguments?.getBoolean("SHOULD_HIDE") ?: false }
    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId") ?: 0 }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private val budgetViewModel by lazy { getViewModel(BudgetViewModel::class.java) }
    private val categoryViewModel by lazy { getViewModel(CategoryViewModel::class.java) }
    private val pluginViewModel by lazy { getViewModel(TransactionPluginViewModel::class.java) }
    private var fileUri: Uri? = null
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
        ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
        transactionType = arguments?.getString("transactionType") ?: ""
        if(isTasker){
            setTaskerBundle()
            setTaskerIcons()
        }
        setIcons()
        setWidgets()
        if(transactionJournalId != 0L){
            updateTransactionSetup()
        }
        setFab()
        setCalculator()
        contextSwitch()
    }

    private fun setTaskerIcons(){
        val variablesFromHost = TaskerPlugin.getRelevantVariableList(requireActivity().intent.extras)
        val iconColor = getCompatColor(R.color.colorAccent)
        descriptionTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = getCompatColor(R.color.md_white_1000)
                })
        transactionAmountTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        currencyTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        dateTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        sourceTextInputTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        sourceExposedTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        destinationTextInputTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        destinationExposedTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        timeTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        piggyTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        categoryTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        tagsTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })
        budgetTasker.setImageDrawable(
                IconicsDrawable(requireContext()).apply{
                    icon(GoogleMaterial.Icon.gmd_label)
                    colorInt = iconColor
                })

        val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item)
        variablesFromHost.forEach { variables ->
            arrayAdapter.add(variables)
        }
        val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Select Variable")
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }

        descriptionTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                description_edittext.setText(itemClicked)
            }
            dialog.show()
        }

        transactionAmountTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                transaction_amount_edittext.setText(itemClicked)
            }
            dialog.show()
        }

        currencyTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                currency_edittext.setText(itemClicked)
                currency = itemClicked ?: ""
            }
            dialog.show()
        }

        dateTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                transaction_date_edittext.setText(itemClicked)
            }
            dialog.show()
        }

        sourceTextInputTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                source_edittext.setText(itemClicked)
            }
            dialog.show()
        }

        sourceExposedTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                source_exposed_dropdown.setText(itemClicked)
            }
            dialog.show()
        }

        destinationTextInputTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                destination_edittext.setText(itemClicked)
            }
            dialog.show()
        }

        destinationExposedTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                destination_exposed_dropdown.setText(itemClicked)
            }
            dialog.show()
        }

        timeTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                time_edittext.setText(itemClicked)
            }
            dialog.show()
        }

        piggyTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                piggy_edittext.setText(itemClicked)
            }
            dialog.show()
        }
        categoryTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                category_edittext.setText(itemClicked)
            }
            dialog.show()
        }

        tagsTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                tags_chip.setText(itemClicked)
            }
            dialog.show()
        }

        budgetTasker.setOnClickListener {
            dialog.setAdapter(arrayAdapter){ _, which ->
                val itemClicked = arrayAdapter.getItem(which)
                budget_edittext.setText(itemClicked)
            }
            dialog.show()
        }
    }

    private fun setTaskerBundle(){
        pluginViewModel.transactionBundle.observe(viewLifecycleOwner){ bundle ->
            if(bundle != null){
                val transactionTypeBundle = bundle.getString("transactionType") ?: ""
                transactionType = transactionTypeBundle

                val transactionDescription = bundle.getString("transactionDescription")
                description_edittext.setText(transactionDescription)

                val transactionAmount = bundle.getString("transactionAmount")
                transaction_amount_edittext.setText(transactionAmount)

                val transactionDateTime = bundle.getString("transactionDateTime")
                transaction_date_edittext.setText(DateTimeUtil.convertIso8601ToHumanDate(transactionDateTime))
                time_edittext.setText(DateTimeUtil.convertIso8601ToHumanTime(transactionDateTime))

                if(piggy_layout.isVisible){
                    val transactionPiggyBank = bundle.getString("transactionPiggyBank")
                    piggy_edittext.setText(transactionPiggyBank)
                }

                val transactionSourceAccount = bundle.getString("transactionSourceAccount")
                val transactionDestinationAccount = bundle.getString("transactionDestinationAccount")

                when {
                    Objects.equals("Withdrawal", transactionType) -> {
                        destination_edittext.setText(transactionDestinationAccount)
                        sourceName = transactionSourceAccount
                        source_exposed_dropdown.setText(sourceName)
                        destinationExposedTasker.isVisible = false
                        sourceTextInputTasker.isVisible = false
                    }
                    Objects.equals("Transfer", transactionType) -> {
                        sourceName = transactionSourceAccount
                        destinationName = transactionDestinationAccount
                        source_exposed_dropdown.setText(sourceName)
                        destination_exposed_dropdown.setText(destinationName)
                        destinationTextInputTasker.isVisible = false
                        sourceTextInputTasker.isVisible = false
                        piggyTasker.isVisible = true
                    }
                    Objects.equals("Deposit", transactionType) -> {
                        source_edittext.setText(transactionSourceAccount)
                        destinationName = transactionDestinationAccount
                        destination_exposed_dropdown.setText(destinationName)
                        sourceExposedTasker.isVisible = false
                        destinationTextInputTasker.isVisible = false
                    }
                }

                val transactionCurrency = bundle.getString("transactionCurrency")
                if(transactionCurrency?.startsWith("%") == false){
                    currencyViewModel.getCurrencyByCode(transactionCurrency ?: "").observe(viewLifecycleOwner){ currencyData ->
                        val currencyAttributes = currencyData[0].currencyAttributes
                        currency_edittext.setText(currencyAttributes?.name + " (" + currencyAttributes?.code + ")")
                    }
                } else {
                    currency_edittext.setText(transactionCurrency)
                }

                val transactionTags = bundle.getString("transactionTags")
                tags_chip.setText(transactionTags)

                val transactionBudget = bundle.getString("transactionBudget")
                budget_edittext.setText(transactionBudget)

                val transactionCategory = bundle.getString("transactionCategory")
                category_edittext.setText(transactionCategory)

                val fileUri = bundle.getString("fileUri")
                if(fileUri != null){
                    val attachmentDataAdapter = arrayListOf<AttachmentData>()
                    attachment_information.isVisible = true
                    attachment_information.layoutManager = LinearLayoutManager(requireContext())
                    attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                    attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                            "", "", FileUtils.getFileName(requireContext(), fileUri.toUri()) ?: "",
                            "", "" , "", 0, "", "", ""), 0, ""))
                    attachment_information.adapter = TransactionAttachmentRecyclerAdapter(attachmentDataAdapter,
                            false) { data: AttachmentData ->
                    }
                }

            }
        }
    }

    private fun setFab(){
        if(transactionJournalId != 0L) {
            addTransactionFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
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
            if(transactionJournalId != 0L){
                updateData()
            } else {
                if(isTasker){
                    taskerPlugin()
                } else {
                    submitData()
                }

            }
        }
    }

    private fun taskerPlugin(){
        val transactionDateTime = if (time_layout.isVisible && selectedTime.isNotBlank()){
            // This is a tasker variable, do not parse
            if(selectedTime.startsWith("%")){
                transaction_date_edittext.getString() + selectedTime
            } else {
                DateTimeUtil.mergeDateTimeToIso8601(transaction_date_edittext.getString(), selectedTime)
            }
        } else {
            transaction_date_edittext.getString()
        }
        if(currency.isBlank()){
            val currencyText = currency_edittext.getString()
            if(currencyText.startsWith("%")){
                currency = currency_edittext.getString()
            } else {
                /* Get content between brackets
                 * For example: Euro(EUR) becomes (EUR)
                 * Then we remove the first and last character to become EUR
                 */
                currency = currency_edittext.getString()
                Regex("\\((.*?)\\)").matches(currency)
                currency.replace("(", "")
                currency.replace(")", "")
            }
        }
        pluginViewModel.transactionType.postValue(transactionType)
        pluginViewModel.transactionDescription.postValue(description_edittext.getString())
        pluginViewModel.transactionAmount.postValue(transaction_amount_edittext.getString())
        pluginViewModel.transactionDateTime.postValue(transactionDateTime)
        pluginViewModel.transactionPiggyBank.postValue(piggyBank)
        pluginViewModel.transactionSourceAccount.postValue(sourceAccount)
        pluginViewModel.transactionDestinationAccount.postValue(destinationAccount)
        pluginViewModel.transactionCurrency.postValue(currency)
        pluginViewModel.transactionTags.postValue(transactionTags)
        pluginViewModel.transactionBudget.postValue(budgetName)
        pluginViewModel.transactionCategory.postValue(categoryName)
        pluginViewModel.fileUri.postValue(fileUri)
        pluginViewModel.removeFragment.postValue(true)
    }

    private fun updateData(){
        val transactionDateTime = if (time_layout.isVisible && selectedTime.isNotBlank()){
            DateTimeUtil.mergeDateTimeToIso8601(transaction_date_edittext.getString(), selectedTime)
        } else {
            transaction_date_edittext.getString()
        }
        transactionViewModel.updateTransaction(transactionJournalId,transactionType, description_edittext.getString(),
                transactionDateTime, transaction_amount_edittext.getString(),
                sourceAccount, destinationAccount, currency, categoryName,
                transactionTags, budgetName, fileUri).observe(viewLifecycleOwner) { transactionResponse->
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
        tags_layout.startIconDrawable = IconicsDrawable(requireContext()).apply{
            icon = FontAwesome.Icon.faw_tags
            colorRes = R.color.md_blue_900
            sizeDp = 24
        }
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        transaction_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_calculator
                    colorRes = R.color.md_blue_grey_400
                    sizeDp = 16
                }, null, null, null)
        transaction_date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        },null, null, null)
        source_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_exchange_alt
            colorRes = R.color.md_green_500
            sizeDp = 24
        },null, null, null)
        val bankTransferIconColorWrap = DrawableCompat.wrap(getCompatDrawable(R.drawable.ic_bank_transfer)!!).mutate()
        DrawableCompat.setTint(bankTransferIconColorWrap, Color.parseColor("#e67a15"))
        destination_edittext.setCompoundDrawablesWithIntrinsicBounds(
                bankTransferIconColorWrap,null, null, null)
        category_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply{
            icon = FontAwesome.Icon.faw_chart_bar
            colorRes = R.color.md_deep_purple_400
            sizeDp = 24
        }, null, null, null)
        piggy_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply{
                    icon = FontAwesome.Icon.faw_piggy_bank
                    colorRes = R.color.md_pink_200
                    sizeDp = 24
                },null, null, null)
        time_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_clock
                    colorRes = R.color.md_red_400
                    sizeDp = 24
                },null, null, null)
        tags_chip.chipTokenizer = SpanChipTokenizer(requireContext(), object : ChipCreator<ChipSpan> {
            override fun configureChip(chip: ChipSpan, chipConfiguration: ChipConfiguration) {
            }

            override fun createChip(context: Context, text: CharSequence, data: Any?): ChipSpan {
                return ChipSpan(requireContext(), text,
                        IconicsDrawable(requireContext()).apply {
                            icon = FontAwesome.Icon.faw_tag
                            sizeDp = 12
                        }, data)
            }

            override fun createChip(context: Context, existingChip: ChipSpan): ChipSpan {
                return ChipSpan(requireContext(), existingChip)
            }
        }, ChipSpan::class.java)
        addTransactionFab.setImageDrawable(IconicsDrawable(requireContext()).apply{
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
        budget_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_gratipay
                    colorRes = R.color.md_amber_300
                    sizeDp = 24
                },null, null, null)
    }

    private fun setCalculator(){
        transaction_amount_layout.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_UP){
                transaction_amount_layout.performClick()
                transactionViewModel.transactionAmount.value = if(transaction_amount_edittext.getString().isEmpty()){
                    "0.0"
                } else {
                    transaction_amount_edittext.getString()
                }
                val calculatorDialog = TransactionCalculatorDialog()
                calculatorDialog.show(parentFragmentManager, "calculatorDialog")
            }
            false
        }
        transaction_amount_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= transaction_amount_edittext.compoundDrawables[0].bounds.width() + 30){
                        transactionViewModel.transactionAmount.value = if(transaction_amount_edittext.getString().isEmpty()){
                            "0.0"
                        } else {
                            transaction_amount_edittext.getString()
                        }
                        val calculatorDialog = TransactionCalculatorDialog()
                        calculatorDialog.show(parentFragmentManager, "calculatorDialog")
                        return true
                    }
                }
                return false
            }

        })
        transactionViewModel.transactionAmount.observe(viewLifecycleOwner){ amount ->
            transaction_amount_edittext.setText(amount)
        }
    }

    private fun setWidgets(){
        showCase(R.string.urge_users_to_click_icons, "transactionIcons", transaction_amount_placeholder_view).show()
        add_attachment_button.setOnClickListener {
            openDocViewer()
        }
        transaction_date_edittext.setText(DateTimeUtil.getTodayDate())
        transaction_date_edittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                transaction_date_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        category_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    category_edittext.compoundDrawables[0].bounds.width()
                    if(event.x <= category_edittext.compoundDrawables[0].bounds.width() + 30){
                        val catDialog = CategoriesDialog()
                        catDialog.show(parentFragmentManager, "categoryDialog")
                        return true
                    }
                }
                return false
            }
        })
        categoryViewModel.categoryName.observe(viewLifecycleOwner) {
            category_edittext.setText(it)
        }
        currencyViewModel.currencyCode.observe(viewLifecycleOwner) {
            currency = it
        }
        currencyViewModel.currencyDetails.observe(viewLifecycleOwner) {
            currency_edittext.setText(it)
        }
        currency_edittext.setOnClickListener{
            CurrencyListBottomSheet().show(parentFragmentManager, "currencyList" )
        }
        tags_chip.addChipTerminator('\n' ,ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        tags_chip.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR)
        tags_chip.enableEditChipOnTouch(false, true)
        tagsViewModel.getAllTags().observe(viewLifecycleOwner) {
            it.forEachIndexed{ _, tagsData ->
                tags.add(tagsData.tagsAttributes?.tag!!)
            }
            val tagsAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, tags)
            tags_chip.threshold = 1
            tags_chip.setAdapter(tagsAdapter)
        }

        currencyViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            if(!isTasker){
                currency = currencyData?.code.toString()
                currency_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
            }
        }
        accountViewModel.emptyAccount.observe(viewLifecycleOwner) {
            if(it == true){
                AlertDialog.Builder(requireContext())
                        .setTitle("No asset accounts found!")
                        .setMessage("We tried searching for an asset account but is unable to find any. Would you like" +
                                "to add an asset account first? ")
                        .setPositiveButton("OK"){ _,_ ->
                            parentFragmentManager.commit {
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
            piggyBankDialog.show(parentFragmentManager, "piggyDialog")
        }
        piggyViewModel.piggyName.observe(viewLifecycleOwner) {
            piggy_edittext.setText(it)
        }
        budget_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    budget_edittext.compoundDrawables[0].bounds.width()
                    if(event.x <= budget_edittext.compoundDrawables[0].bounds.width() + 30){
                        val budgetDialog = BudgetSearchDialog()
                        budgetDialog.show(parentFragmentManager, "budgetDialog")
                        return true
                    }
                }
                return false
            }
        })
        budgetViewModel.budgetName.observe(viewLifecycleOwner) { name ->
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
            // Do not use this yet as there is a bug
            // https://github.com/material-components/material-components-android/issues/1508
            /*MaterialTimePicker().apply {
                setTimeFormat(TimeFormat.CLOCK_24H)
                setListener { materialTimePickerListener ->
                    selectedTime = "${materialTimePickerListener.hour}:${materialTimePickerListener.minute}"
                    time_edittext.setText(selectedTime)
                }
                show(parentFragmentManager, "timePickerDialog")
            }*/
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(requireContext(), TimePickerDialog.OnTimeSetListener {
                _, selectedHour, selectedMin ->
                // We don't have to be really accurate down to seconds.
                val min = if(selectedMin < 10){
                    "0$selectedMin"
                } else {
                    selectedMin.toString()
                }
                selectedTime = "$selectedHour:$min"
                time_edittext.setText(selectedTime)
            },hour, minute, true).show()
        }
    }

    private fun contextSwitch(){
        when {
            Objects.equals(transactionType, "Transfer") -> accountViewModel.getAccountNameByType("asset")
                    .observe(viewLifecycleOwner) { transferData ->
                        source_exposed_menu.isVisible = true
                        sourceExposedTasker.isVisible = true
                        source_layout.isGone = true
                        sourceTextInputTasker.isGone = true
                        destination_layout.isGone = true
                        destinationTextInputTasker.isGone = true
                        destination_exposed_menu.isVisible = true
                        destinationExposedTasker.isVisible = true
                        piggy_layout.isVisible = true
                        piggyTasker.isVisible = true
                        spinnerAdapter = ArrayAdapter(requireContext(),
                                R.layout.cat_exposed_dropdown_popup_item, transferData)
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        destination_exposed_dropdown.setAdapter(spinnerAdapter)
                        source_exposed_dropdown.setAdapter(spinnerAdapter)
                        ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                    }
            Objects.equals(transactionType, "Deposit") -> zipLiveData(accountViewModel.getAccountNameByType("revenue"),
                    accountViewModel.getAccountNameByType("asset")).observe(viewLifecycleOwner ) {
                // Asset account, spinner
                spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, it.second)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                destination_exposed_dropdown.setAdapter(spinnerAdapter)
                destination_layout.isVisible = false
                destinationTextInputTasker.isVisible = false
                source_exposed_menu.isVisible = false
                sourceExposedTasker.isVisible = false
                // Revenue account, autocomplete
                val autocompleteAdapter = ArrayAdapter(requireContext(),
                        R.layout.cat_exposed_dropdown_popup_item, it.first)
                source_edittext.threshold = 1
                source_edittext.setAdapter(autocompleteAdapter)
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
            else -> {
                zipLiveData(accountViewModel.getAccountNameByType("asset"),
                        accountViewModel.getAccountNameByType("expense")).observe(viewLifecycleOwner) { accountNames ->
                    source_layout.isVisible = false
                    destination_exposed_menu.isVisible = false
                    sourceTextInputTasker.isVisible = false
                    destinationExposedTasker.isVisible = false
                    // Spinner for source account
                    spinnerAdapter = ArrayAdapter(requireContext(),
                            R.layout.cat_exposed_dropdown_popup_item, accountNames.first)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    source_exposed_dropdown.setAdapter(spinnerAdapter)
                    // This is used for auto complete for destination account
                    val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, accountNames.second)
                    destination_edittext.threshold = 1
                    destination_edittext.setAdapter(autocompleteAdapter)
                    ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                }
            }
        }
    }

    private fun submitData() {
        val transactionDateTime = if (time_layout.isVisible && selectedTime.isNotBlank()){
            DateTimeUtil.mergeDateTimeToIso8601(transaction_date_edittext.getString(), selectedTime)
        } else {
            transaction_date_edittext.getString()
        }
        transactionViewModel.addTransaction(transactionType, description_edittext.getString(),
                transactionDateTime, piggyBank, transaction_amount_edittext.getString(),
                sourceAccount, destinationAccount,
                currency, categoryName, transactionTags, budgetName, fileUri).observe(viewLifecycleOwner) { transactionResponse ->
            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            val errorMessage = transactionResponse.getErrorMessage()
            if (transactionResponse.getResponse() != null) {
                toastSuccess(resources.getString(R.string.transaction_added))
                handleBack()
            } else if (errorMessage != null) {
                toastError(errorMessage)
            } else if (transactionResponse.getError() != null) {
                when {
                    Objects.equals("Transfer", transactionType) -> {
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
                toastOffline(getString(R.string.data_will_be_deleted_later, transactionType))
                handleBack()
            }
        }
    }

    private fun updateTransactionSetup(){
        transactionViewModel.getTransactionByJournalId(transactionJournalId).observe(viewLifecycleOwner) { transactionData ->
            val transactionAttributes = transactionData[0]
            description_edittext.setText(transactionAttributes.description)
            transaction_amount_edittext.setText(abs(transactionAttributes.amount).toString())
            budget_edittext.setText(transactionAttributes.budget_name)
            currencyViewModel.getCurrencyByCode(transactionAttributes.currency_code).observe(viewLifecycleOwner) { currencyData ->
                val currencyAttributes = currencyData[0].currencyAttributes
                currency_edittext.setText(currencyAttributes?.name + " (" + currencyAttributes?.code + ")")
            }
            currency = transactionAttributes.currency_code
            transaction_date_edittext.setText(DateTimeUtil.convertIso8601ToHumanDate(transactionAttributes.date))
            category_edittext.setText(transactionAttributes.category_name)
            time_edittext.setText(DateTimeUtil.convertIso8601ToHumanTime(transactionAttributes.date))
            if(transactionAttributes.tags.isNotEmpty()){
                tags_chip.setText(transactionAttributes.tags)
            }
            when {
                Objects.equals("Withdrawal", transactionType) -> {
                    destination_edittext.setText(transactionAttributes.destination_name)
                    sourceName = transactionAttributes.source_name
                    source_exposed_dropdown.setText(sourceName)
                }
                Objects.equals("Transfer", transactionType) -> {
                    sourceName = transactionAttributes.source_name
                    destinationName = transactionAttributes.destination_name
                    source_exposed_dropdown.setText(sourceName)
                    destination_exposed_dropdown.setText(destinationName)
                }
                Objects.equals("Deposit", transactionType) -> {
                    source_edittext.setText(transactionAttributes.source_name)
                    destinationName = transactionAttributes.destination_name
                    destination_exposed_dropdown.setText(destinationName)
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
                    fileUri = resultData.data
                    val attachmentDataAdapter = arrayListOf<AttachmentData>()
                    attachment_information.isVisible = true
                    attachment_information.layoutManager = LinearLayoutManager(requireContext())
                    attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                    attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                            "", "", FileUtils.getFileName(requireContext(), fileUri ?: Uri.EMPTY) ?: "",
                            "", "" , "", 0, "", "", ""), 0, ""))
                    attachment_information.adapter = TransactionAttachmentRecyclerAdapter(attachmentDataAdapter, false) { data: AttachmentData ->
                    }

                }
            }
        }
    }

    override fun handleBack() {
        if(nastyHack){
            parentFragmentManager.popBackStack()
            fragmentContainer.isVisible = true
            fragment_add_transaction_root.isVisible = false
            requireActivity().findViewById<FloatingActionButton>(R.id.addTransactionFab).isVisible = true
        } else {
            if(isTasker){
                requireActivity().onBackPressed()
            } else {
                requireActivity().finish()
            }
        }
    }
}