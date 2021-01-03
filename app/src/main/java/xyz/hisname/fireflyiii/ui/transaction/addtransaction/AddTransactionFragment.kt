package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
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
import kotlinx.android.synthetic.main.activity_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.fragment_add_transaction.currency_edittext
import kotlinx.android.synthetic.main.fragment_add_transaction.description_edittext
import kotlinx.android.synthetic.main.fragment_add_transaction.expansionLayout
import me.toptas.fancyshowcase.FancyShowCaseQueue
import net.dinglisch.android.tasker.TaskerPlugin
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.MarkdownViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.search.AccountSearchDialog
import xyz.hisname.fireflyiii.ui.account.search.AccountSearchViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.categories.CategoriesDialog
import xyz.hisname.fireflyiii.ui.categories.CategoriesDialogViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyBottomSheetViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.transaction.search.DescriptionSearch
import xyz.hisname.fireflyiii.ui.transaction.search.DescriptionViewModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File
import java.util.*

class AddTransactionFragment: BaseFragment() {

    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId") ?: 0 }
    private val transactionActivity by lazy { arguments?.getBoolean("FROM_TRANSACTION_ACTIVITY") }
    private val isTasker by lazy { arguments?.getBoolean("isTasker") ?: false }
    private val isFromNotification by lazy { requireActivity().intent.extras?.getBoolean("isFromNotification") ?: false }
    private val transactionType by lazy { arguments?.getString("transactionType") ?: "" }

    private val addTransactionViewModel by lazy { getViewModel(AddTransactionViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val categorySearch by lazy { getViewModel(CategoriesDialogViewModel::class.java) }
    private val descriptionSearch by lazy { getViewModel(DescriptionViewModel::class.java) }
    private val accountSearchViewModel by lazy { getViewModel(AccountSearchViewModel::class.java) }

    private lateinit var fileUri: Uri
    private var selectedTime = ""
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private val attachmentDataAdapter by lazy { arrayListOf<AttachmentData>() }
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressBar.animateView(addTransactionProgress, View.VISIBLE, 0.4f, 200)
        if(isTasker){
            addTransactionViewModel.transactionBundle.observe(viewLifecycleOwner) { bundle ->
                addTransactionViewModel.parseBundle(bundle)
            }
            addSplit.isGone = true
            removeSplit.isGone = true
            addTransactionViewModel.isFromTasker.postValue(true)
        }
        if(isFromNotification){
            requireActivity().intent.extras?.let {
                addTransactionViewModel.parseBundle(it)
            }
        }
        setIcons()
        setWidgets()
        if(transactionJournalId != 0L){
            addSplit.isGone = true
            removeSplit.isGone = true
            addTransactionViewModel.getTransactionFromJournalId(transactionJournalId)
        }
        setFab()
        setCalculator()
        contextSwitch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0))
                attachmentItemAdapter.add(fileUri)
                attachment_information.adapter?.notifyDataSetChanged()
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
                attachment_information.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun showTaskerVariable(editText: EditText, shouldAppend: Boolean = false){
        val variablesFromHost = TaskerPlugin.getRelevantVariableList(requireActivity().intent.extras)
        val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item)
        variablesFromHost.forEach { variables ->
            arrayAdapter.add(variables)
        }
        val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Select Variable")
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
        dialog.setAdapter(arrayAdapter){ _, which ->
            val itemClicked = arrayAdapter.getItem(which)
            if(shouldAppend){
                editText.append(itemClicked + "\n")
            } else {
                editText.setText(itemClicked)
            }
        }
        dialog.show()
    }

    private fun setTaskerIcons(): Drawable? {
        return if(isTasker){
            IconicsDrawable(requireContext()).apply {
                icon = GoogleMaterial.Icon.gmd_label
                colorRes = R.color.colorAccent
                sizeDp = 24
            }
        } else {
            null
        }
    }

    private fun setUi() {
        addTransactionViewModel.transactionCurrency.observe(viewLifecycleOwner) { transactionCurrency ->
            currency_edittext.setText(transactionCurrency)
        }

        addTransactionViewModel.transactionDescription.observe(viewLifecycleOwner) { transactionDescription ->
            description_edittext.setText(transactionDescription)
        }

        addTransactionViewModel.transactionAmount.observe(viewLifecycleOwner) { transactionAmount ->
            transaction_amount_edittext.setText(transactionAmount)
        }

        addTransactionViewModel.transactionDate.observe(viewLifecycleOwner) { transactionDate ->
            transaction_date_edittext.setText(transactionDate)
        }

        addTransactionViewModel.transactionTime.observe(viewLifecycleOwner) { transactionTime ->
            time_edittext.setText(transactionTime)
        }

        if (piggy_exposed_menu.isVisible) {
            addTransactionViewModel.transactionPiggyBank.observe(viewLifecycleOwner) { transactionPiggyBank ->
                piggy_exposed_dropdown.setText(transactionPiggyBank)

            }
        }

        addTransactionViewModel.transactionTags.observe(viewLifecycleOwner) { transactionTags ->
            tags_chip.setText(transactionTags)
        }

        addTransactionViewModel.transactionBudget.observe(viewLifecycleOwner) { transactionBudget ->
            budget_exposed_dropdown.setText(transactionBudget)
        }

        addTransactionViewModel.transactionCategory.observe(viewLifecycleOwner) { transactionCategory ->
            category_edittext.setText(transactionCategory)
        }

        addTransactionViewModel.transactionNote.observe(viewLifecycleOwner) { transactionNote ->
            note_edittext.setText(transactionNote)
        }

        addTransactionViewModel.fileUri.observe(viewLifecycleOwner){ uri ->
            uri.forEach { uriArray ->
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), uriArray) ?: "",
                        "", "", "", 0, "", "", ""), 0))
            }
            attachmentItemAdapter.addAll(uri)
            attachment_information.adapter?.notifyDataSetChanged()
        }
    }

    private fun setFab(){
        addTransactionViewModel.saveData.observe(viewLifecycleOwner){
            hideKeyboard()
            val piggyBank = if(piggy_exposed_dropdown.isBlank()){
                null
            } else {
                piggy_exposed_dropdown.getString()
            }
            val categoryName = if(category_edittext.isBlank()){
                null
            } else {
                category_edittext.getString()
            }
            val transactionTags = if(tags_chip.allChips.isNullOrEmpty()){
                null
            } else {
                // Remove [ and ] from beginning and end of string
                val beforeTags = tags_chip.allChips.toString().substring(1)
                beforeTags.substring(0, beforeTags.length - 1)
            }
            val budgetName = if(budget_exposed_dropdown.isBlank()){
                null
            } else {
                budget_exposed_dropdown.getString()
            }
            var sourceAccount = ""
            var destinationAccount = ""
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
                updateData(piggyBank, sourceAccount, destinationAccount, categoryName, transactionTags, budgetName)
            } else {
                if(isTasker){
                    taskerPlugin(piggyBank, sourceAccount, destinationAccount, categoryName, transactionTags, budgetName)
                } else {
                    submitData(piggyBank, sourceAccount, destinationAccount, categoryName, transactionTags, budgetName)
                }

            }
        }
    }

    private fun taskerPlugin(piggyBank: String?, sourceAccount: String, destinationAccount: String,
                             categoryName: String?, transactionTags: String?, budgetName: String?){
        val currencyText = currency_edittext.getString()
        if(currencyText.startsWith("%")){
            addTransactionViewModel.currency = currency_edittext.getString()
        } else {
            /* Get content between brackets
             * For example: Euro(EUR) becomes (EUR)
             * Then we remove the first and last character and it becomes EUR
             */
            addTransactionViewModel.currency = currency_edittext.getString()
            val regex = "(?<=\\().+?(?=\\))".toRegex()
            val regexReplaced = regex.find(addTransactionViewModel.currency)
            regexReplaced?.value
            addTransactionViewModel.currency = regexReplaced?.value ?: ""
        }
        addTransactionViewModel.transactionType.postValue(transactionType)
        addTransactionViewModel.transactionDescription.postValue(description_edittext.getString())
        addTransactionViewModel.transactionAmount.postValue(transaction_amount_edittext.getString())
        addTransactionViewModel.transactionDate.postValue(transaction_date_edittext.getString())
        addTransactionViewModel.transactionTime.postValue(time_edittext.getString())
        addTransactionViewModel.transactionPiggyBank.postValue(piggyBank)
        addTransactionViewModel.transactionSourceAccount.postValue(sourceAccount)
        addTransactionViewModel.transactionDestinationAccount.postValue(destinationAccount)
        addTransactionViewModel.transactionCurrency.postValue(addTransactionViewModel.currency)
        addTransactionViewModel.transactionTags.postValue(transactionTags)
        addTransactionViewModel.transactionBudget.postValue(budgetName)
        addTransactionViewModel.transactionCategory.postValue(categoryName)
        addTransactionViewModel.transactionNote.postValue(note_edittext.getString())
        addTransactionViewModel.fileUri.postValue(attachmentItemAdapter)
        addTransactionViewModel.removeFragment.postValue(true)
    }

    private fun setIcons(){
        tags_chip.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_tags
            colorRes = R.color.md_green_400
            sizeDp = 24
        },null, setTaskerIcons(), null)

        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, setTaskerIcons(), null)
        transaction_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_calculator
                    colorRes = R.color.md_blue_grey_400
                    sizeDp = 16
                }, null, setTaskerIcons(), null)
        transaction_date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        },null, setTaskerIcons(), null)
        source_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_exchange_alt
            colorRes = R.color.md_green_500
            sizeDp = 24
        },null, setTaskerIcons(), null)
        val bankTransferIconColorWrap = DrawableCompat.wrap(getCompatDrawable(R.drawable.ic_bank_transfer)!!).mutate()
        DrawableCompat.setTint(bankTransferIconColorWrap, Color.parseColor("#e67a15"))
        destination_edittext.setCompoundDrawablesWithIntrinsicBounds(
                bankTransferIconColorWrap,null, setTaskerIcons(), null)
        category_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply{
            icon = FontAwesome.Icon.faw_chart_bar
            colorRes = R.color.md_deep_purple_400
            sizeDp = 24
        }, null, setTaskerIcons(), null)
        time_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_clock
                    colorRes = R.color.md_red_400
                    sizeDp = 24
                },null, setTaskerIcons(), null)
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
        description_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_description
                    colorRes = R.color.md_amber_300
                    sizeDp = 20
                },null, setTaskerIcons(), null)
        if(isTasker){
            tags_layout.endIconDrawable = setTaskerIcons()
            note_edittext.setCompoundDrawablesWithIntrinsicBounds(
                    null,null, setTaskerIcons(), null)
        }
    }

    private fun setCalculator(){
        transaction_amount_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= transaction_amount_edittext.compoundDrawables[0].bounds.width() + 30){
                        addTransactionViewModel.transactionAmount.value = if(transaction_amount_edittext.getString().isEmpty()){
                            "0.0"
                        } else {
                            transaction_amount_edittext.getString()
                        }
                        val calculatorDialog = TransactionCalculatorDialog()
                        calculatorDialog.show(parentFragmentManager, "calculatorDialog")
                        return true
                    } else if(transaction_amount_edittext.compoundDrawables[2] != null &&
                            event.rawX >= (transaction_amount_edittext.right -
                                    transaction_amount_edittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(transaction_amount_edittext)
                    }
                }
                return false
            }
        })
        addTransactionViewModel.transactionAmount.observe(viewLifecycleOwner){ amount ->
            transaction_amount_edittext.setText(amount)
        }
    }

    private fun setWidgets(){
        val queue = FancyShowCaseQueue()
        if(transactionActivity == true){
            queue.add(showCase(R.string.transactions_create_switch_box, "bottomNavigationShowCase",
                           requireActivity().findViewById(R.id.transactionBottomView), false))
        }
        queue.add(showCase(R.string.urge_users_to_click_icons, "transactionIcons",
                transaction_amount_placeholder_view))
        queue.show()
        add_attachment_button.setOnClickListener {
            attachmentDialog()
        }
        transaction_date_edittext.setText(DateTimeUtil.getTodayDate())
        transaction_date_edittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                transaction_date_edittext.performClick()
                if (transaction_date_edittext.compoundDrawables[2] != null &&
                        event.rawX >= (transaction_date_edittext.right -
                                transaction_date_edittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(transaction_date_edittext)
                } else {
                    val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    val picker = materialDatePicker.build()
                    picker.show(parentFragmentManager, picker.toString())
                    picker.addOnPositiveButtonClickListener { time ->
                        transaction_date_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
                    }
                }
            }
            false
        }
        tags_chip.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                tags_chip.performClick()
                if (tags_chip.compoundDrawables[2] != null &&
                        event.rawX >= (tags_chip.right -
                                tags_chip.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(tags_chip, true)
                }
            }
            false
        }
        category_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= category_edittext.compoundDrawables[0].bounds.width() + 30){
                        val catDialog = CategoriesDialog()
                        catDialog.show(parentFragmentManager, "categoryDialog")
                        return true
                    } else if(category_edittext.compoundDrawables[2] != null &&
                            event.rawX >= (category_edittext.right -
                                    category_edittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(category_edittext)
                    }
                }
                return false
            }
        })
        categorySearch.categoryName.observe(viewLifecycleOwner) {
            category_edittext.setText(it)
        }
        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currency ->
            addTransactionViewModel.currency = currency
        }
        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) {
            currency_edittext.setText(it)
        }
        currency_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(currency_edittext.compoundDrawables[2] != null &&
                            event.rawX >= (currency_edittext.right -
                                    currency_edittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(currency_edittext)
                        addTransactionViewModel.currency = currency_edittext.getString()
                    } else {
                        CurrencyListBottomSheet().show(parentFragmentManager, "currencyList" )
                    }
                }
                return true
            }
        })
        tags_chip.addChipTerminator('\n' ,ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        tags_chip.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR)
        tags_chip.enableEditChipOnTouch(false, true)
        tags_chip.doAfterTextChanged { editable ->
            addTransactionViewModel.getTags(editable.toString()).observe(viewLifecycleOwner){ tags ->
                val tagsAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, tags)
                tags_chip.threshold = 1
                tags_chip.setAdapter(tagsAdapter)
            }
        }
        if(isTasker){
            source_exposed_menu.endIconDrawable = setTaskerIcons()
            source_exposed_menu.setEndIconOnClickListener {
                showTaskerVariable(source_exposed_dropdown)
            }
            destination_exposed_menu.endIconDrawable = setTaskerIcons()
            destination_exposed_menu.setEndIconOnClickListener {
                showTaskerVariable(destination_exposed_dropdown)
            }
            budget_exposed_menu.endIconDrawable = setTaskerIcons()
            budget_exposed_menu.setEndIconOnClickListener {
                showTaskerVariable(budget_exposed_dropdown)
            }
            piggy_exposed_menu.endIconDrawable = setTaskerIcons()
            piggy_exposed_menu.setEndIconOnClickListener {
                showTaskerVariable(piggy_exposed_dropdown)
            }
        }
        addTransactionViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
            if(!isTasker){
                currency_edittext.setText(defaultCurrency)
            } else if(!currency_edittext.getString().startsWith("%")) {
                // Is not a tasker variable
                currency_edittext.setText(defaultCurrency)
            }
        }

        addTransactionViewModel.getPiggyBank().observe(viewLifecycleOwner){ budget ->
            val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, budget)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            piggy_exposed_dropdown.setAdapter(spinnerAdapter)
        }

        addTransactionViewModel.getBudget().observe(viewLifecycleOwner){ budget ->
            val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, budget)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            budget_exposed_dropdown.setAdapter(spinnerAdapter)
        }

        destination_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= destination_edittext.compoundDrawables[0].bounds.width() + 30){
                        val accountDialog = AccountSearchDialog()
                        accountDialog.arguments = bundleOf("accountType" to "expense")
                        accountDialog.show(parentFragmentManager, "accountDialog")
                        accountSearchViewModel.accountName.observe(viewLifecycleOwner){ account ->
                            destination_edittext.setText(account)
                        }
                        return true
                    } else if(destination_edittext.compoundDrawables[2] != null &&
                            event.rawX >= (destination_edittext.right -
                                    destination_edittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(destination_edittext)
                    }
                }
                return false
            }
        })

        source_edittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= source_edittext.compoundDrawables[0].bounds.width() + 30){
                        val accountDialog = AccountSearchDialog()
                        accountDialog.arguments = bundleOf("accountType" to "revenue")
                        accountDialog.show(parentFragmentManager, "accountDialog")
                        accountSearchViewModel.accountName.observe(viewLifecycleOwner){ account ->
                            source_edittext.setText(account)
                        }
                        return true
                    } else if(source_edittext.compoundDrawables[2] != null &&
                            event.rawX >= (source_edittext.right -
                                    source_edittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(source_edittext)
                    }
                }
                return false
            }
        })
        expansionLayout.addListener { _, expanded ->
            if(expanded){
                if (piggy_exposed_menu.isVisible){
                    dialog_add_transaction_layout.post {
                        dialog_add_transaction_layout.smoothScrollTo(0, piggy_exposed_menu.bottom)
                    }
                    dialog_add_transaction_layout.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                        if(scrollY == piggy_exposed_menu.bottom){
                            showCase(R.string.transactions_create_transfer_ffInput_piggy_bank_id,
                                    "transactionPiggyShowCase", piggy_exposed_menu, false).show()
                        }
                    }
                }
            }
        }
        time_edittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                time_edittext.performClick()
                if (time_edittext.compoundDrawables[2] != null &&
                        event.rawX >= (time_edittext.right -
                                time_edittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(time_edittext)
                } else {
                    val materialTimePicker = MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .build()
                    materialTimePicker.show(parentFragmentManager, "timePickerDialog")
                    materialTimePicker.addOnPositiveButtonClickListener { _ ->
                        val min = if(materialTimePicker.minute < 10){
                            "0${materialTimePicker.minute}"
                        } else {
                            materialTimePicker.minute.toString()
                        }
                        val hour = if(materialTimePicker.hour < 10){
                            "0${materialTimePicker.hour}"
                        } else {
                            materialTimePicker.hour.toString()
                        }
                        selectedTime = "${hour}:${min}"
                        time_edittext.setText(selectedTime)
                    }
                }
            }
            false
        }
        description_edittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                description_edittext.performClick()
                if (description_edittext.compoundDrawables[2] != null &&
                        event.rawX >= (description_edittext.right -
                                description_edittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(description_edittext)
                } else if(event.x <= description_edittext.compoundDrawables[0].bounds.width() + 30){
                    val transactionDescription = DescriptionSearch()
                    transactionDescription.show(parentFragmentManager, "descriptionDialog")
                    descriptionSearch.transactionName.observe(viewLifecycleOwner){ search ->
                        description_edittext.setText(search)
                    }
                }
            }
            false
        }
        description_edittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getTransactionByDescription(editable.toString()).observe(viewLifecycleOwner){ list ->
                val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, list)
                description_edittext.setAdapter(adapter)
            }
        }
        destination_edittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getAccountByNameAndType("expense", editable.toString()).observe(viewLifecycleOwner){ list ->
                val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, list)
                destination_edittext.setAdapter(autocompleteAdapter)
            }
        }
        category_edittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getCategory(editable.toString()).observe(viewLifecycleOwner) { dataToDisplay ->
                val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, dataToDisplay)
                category_edittext.setAdapter(autocompleteAdapter)
            }
        }
        source_edittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getAccountByNameAndType("revenue", editable.toString()).observe(viewLifecycleOwner){ list ->
                val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, list)
                source_edittext.setAdapter(autocompleteAdapter)
            }
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            note_edittext.setText(markdownText)
        }
        note_edittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                note_edittext.performClick()
                if (note_edittext.compoundDrawables[2] != null &&
                        event.rawX >= (note_edittext.right -
                                note_edittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(note_edittext)
                } else {
                    markdownViewModel.markdownText.postValue(note_edittext.getString())
                    // This is called when in activity
                    if(bigger_fragment_container != null){
                        parentFragmentManager.commit {
                            replace(R.id.bigger_fragment_container, MarkdownFragment())
                            addToBackStack(null)
                        }
                    } else {
                        // This is called in fragment / tasker
                        parentFragmentManager.commit {
                            replace(R.id.transactionPagerRoot, MarkdownFragment())
                            addToBackStack(null)
                        }
                    }
                }
            }
            true
        }
        attachment_information.layoutManager = LinearLayoutManager(requireContext())
        attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        attachment_information.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            attachment_information.adapter?.notifyDataSetChanged()
        }) { another: Int -> }
        setUi()
        addSplit.setOnClickListener {
            addTransactionViewModel.increaseTab.postValue(true)
        }
        removeSplit.setOnClickListener {
            addTransactionViewModel.decreaseTab.postValue(true)
        }
        if(addTransactionViewModel.numTabs > 1){
            removeSplit.isVisible = true
        }
    }

    private fun contextSwitch(){
        addTransactionViewModel.getAccounts().observe(viewLifecycleOwner){ accounts ->
            if(transactionType.contentEquals("Transfer")){
                source_exposed_menu.isVisible = true
                source_layout.isGone = true
                destination_layout.isGone = true
                destination_exposed_menu.isVisible = true
                piggy_exposed_menu.isVisible = true
                val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, accounts)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                destination_exposed_dropdown.setAdapter(spinnerAdapter)
                source_exposed_dropdown.setAdapter(spinnerAdapter)
                addTransactionViewModel.transactionDestinationAccount.observe(viewLifecycleOwner){ transactionDestinationAccount ->
                    destination_exposed_dropdown.setText(transactionDestinationAccount)
                }
                addTransactionViewModel.transactionSourceAccount.observe(viewLifecycleOwner){ transactionSourceAccount ->
                    source_exposed_dropdown.setText(transactionSourceAccount)
                }
            } else if(transactionType.contentEquals("Deposit")){
                val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, accounts)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                destination_exposed_dropdown.setAdapter(spinnerAdapter)
                destination_layout.isGone = true
                source_exposed_menu.isGone = true
                addTransactionViewModel.transactionDestinationAccount.observe(viewLifecycleOwner){ transactionDestinationAccount ->
                    destination_exposed_dropdown.setText(transactionDestinationAccount)
                }
                addTransactionViewModel.transactionSourceAccount.observe(viewLifecycleOwner){ transactionSourceAccount ->
                    source_edittext.setText(transactionSourceAccount)
                }
            } else if(transactionType.contentEquals("Withdrawal")){
                source_layout.isGone = true
                destination_exposed_menu.isGone = true
                // Spinner for source account
                val spinnerAdapter = ArrayAdapter(requireContext(),
                        R.layout.cat_exposed_dropdown_popup_item, accounts)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                source_exposed_dropdown.setAdapter(spinnerAdapter)
                addTransactionViewModel.transactionDestinationAccount.observe(viewLifecycleOwner){ transactionDestinationAccount ->
                    destination_edittext.setText(transactionDestinationAccount)
                }
                addTransactionViewModel.transactionSourceAccount.observe(viewLifecycleOwner){ transactionSourceAccount ->
                    source_exposed_dropdown.setText(transactionSourceAccount)
                }
            }
            ProgressBar.animateView(addTransactionProgress, View.GONE, 0f, 200)
        }
    }

    private fun submitData(piggyBank: String?, sourceAccount: String, destinationAccount: String,
                           categoryName: String?, transactionTags: String?, budgetName: String?) {
        addTransactionViewModel.addTransaction(transactionType, description_edittext.getString(),
                transaction_date_edittext.getString(), selectedTime, piggyBank, transaction_amount_edittext.getString(),
                sourceAccount, destinationAccount, categoryName, transactionTags, budgetName, attachmentItemAdapter,
                note_edittext.getString())
    }

    private fun updateData(piggyBank: String?, sourceAccount: String, destinationAccount: String,
                           categoryName: String?, transactionTags: String?, budgetName: String?){
        ProgressBar.animateView(addTransactionProgress, View.VISIBLE, 0.4f, 200)
        addTransactionViewModel.updateTransaction(transactionJournalId, transactionType,
                description_edittext.getString(), transaction_date_edittext.getString(),
                selectedTime, piggyBank, transaction_amount_edittext.getString(),
                sourceAccount, destinationAccount, categoryName,
                transactionTags, budgetName, note_edittext.getString()).observe(viewLifecycleOwner){ response ->
            addTransactionViewModel.isLoading.postValue(false)
            ProgressBar.animateView(addTransactionProgress, View.GONE, 0f, 200)
            if(response.first){
                toastSuccess(response.second)
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                toastInfo(response.second)
            }
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
}