package xyz.hisname.fireflyiii.ui.transaction

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.hootsuite.nachos.ChipConfiguration
import com.hootsuite.nachos.chip.ChipSpan
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.dialog_add_transaction.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.receiver.TransactionReceiver
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.repository.category.CategoryViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.piggybank.PiggyViewModel
import xyz.hisname.fireflyiii.repository.transaction.TransactionsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.util.animation.CircularReveal
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*
import android.content.Intent
import androidx.core.os.bundleOf
import com.hootsuite.nachos.chip.ChipCreator
import xyz.hisname.fireflyiii.repository.tags.TagsViewModel
import kotlin.collections.ArrayList


class AddTransactionDialog: BaseDialog() {

    private val categoryViewModel by lazy { getViewModel(CategoryViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private val billViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private val piggyViewModel by lazy { getViewModel(PiggyViewModel::class.java) }
    private val tagsViewModel by lazy { getViewModel(TagsViewModel::class.java) }
    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
    private val transactionType by lazy { arguments?.getString("transactionType") ?: "" }
    private val transactionId by lazy { arguments?.getLong("transactionId") ?: 0 }
    private var currency = ""
    private var accounts = ArrayList<String>()
    private var tags = ArrayList<String>()
    private var sourceAccounts = ArrayList<String>()
    private var destinationAccounts = ArrayList<String>()
    private var piggyBankList = ArrayList<String>()
    private val bill = ArrayList<String>()
    private var billName: String? = ""
    private var piggyBank: String? = ""
    private var categoryName: String? = ""
    private var transactionTags: String? = ""
    private var sourceAccount = ""
    private var destinationAccount = ""
    private lateinit var spinnerAdapter: ArrayAdapter<Any>
    private var sourceName: String? = ""
    private var destinationName: String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CircularReveal(dialog_add_transaction_layout).showReveal(revealX, revealY)
        contextSwitch()
        setIcons()
        setWidgets()
        if(transactionId != 0L){
            updateTransactionSetup()
        }
        addTransactionFab.setOnClickListener {
            ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
            hideKeyboard()
            billName = if(bill_edittext.isBlank()){
                null
            } else {
                bill_edittext.getString()
            }
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
            when {
                Objects.equals("Withdrawal", transactionType) -> {
                    sourceAccount = source_spinner.selectedItem.toString()
                    destinationAccount = destination_edittext.getString()
                }
                Objects.equals("Transfer", transactionType) -> {
                    sourceAccount = source_spinner.selectedItem.toString()
                    destinationAccount = destination_spinner.selectedItem.toString()
                }
                Objects.equals("Deposit", transactionType) -> {
                    sourceAccount = source_edittext.getString()
                    destinationAccount = destination_spinner.selectedItem.toString()
                }
            }
            if(transactionId != 0L){
                updateData()
            } else {
                submitData()
            }
        }
    }

    private fun updateTransactionSetup(){
        transactionViewModel.getTransactionById(transactionId).observe(this, Observer {
            val transactionAttributes = it[0].transactionAttributes
            description_edittext.setText(transactionAttributes?.description)
            transaction_amount_edittext.setText(Math.abs(transactionAttributes?.amount
                    ?: 0.toDouble()).toString())
            currencyViewModel.getCurrencyByCode(transactionAttributes?.currency_code.toString()).observe(this, Observer { currencyData ->
                val currencyAttributes = currencyData[0].currencyAttributes
                currency_edittext.setText(currencyAttributes?.name + " (" + currencyAttributes?.code + ")")
            })
            currency = transactionAttributes?.currency_code.toString()
            transaction_date_edittext.setText(transactionAttributes?.date)
            bill_edittext.setText(transactionAttributes?.bill_name)
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
        })
    }

    private fun setIcons(){
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                     .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                     .sizeDp(24),null, null, null)
         transaction_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                 IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_dollar_sign)
                         .color(ContextCompat.getColor(requireContext(), R.color.md_yellow_A700))
                         .sizeDp(16),null, null, null)
         transaction_date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                 .icon(FontAwesome.Icon.faw_calendar)
                 .color(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
                 .sizeDp(24),null, null, null)
         source_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                 .icon(FontAwesome.Icon.faw_exchange_alt).sizeDp(24),null, null, null)
         destination_edittext.setCompoundDrawablesWithIntrinsicBounds(
                 ContextCompat.getDrawable(requireContext(), R.drawable.ic_bank_transfer),null, null, null)
         bill_edittext.setCompoundDrawablesWithIntrinsicBounds(
                 IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                         .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                         .sizeDp(24),null, null, null)
         category_edittext.setCompoundDrawablesWithIntrinsicBounds(
                 IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_chart_bar)
                         .color(ContextCompat.getColor(requireContext(), R.color.md_deep_purple_400))
                         .sizeDp(24),null, null, null)
         piggy_edittext.setCompoundDrawablesWithIntrinsicBounds(
                 IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_piggy_bank)
                         .color(ContextCompat.getColor(requireContext(), R.color.md_pink_200))
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
        placeHolderToolbar.navigationIcon = navIcon
        addTransactionFab.apply{
            setImageDrawable(IconicsDrawable(requireContext())
                    .icon(FontAwesome.Icon.faw_save)
                    .color(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight))
                    .sizeDp(16))
            setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorPrimaryDark))
        }
    }

     private fun setWidgets(){
         transaction_date_edittext.setText(DateTimeUtil.getTodayDate())
         val calendar = Calendar.getInstance()
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
             DatePickerDialog(requireContext(), transactionDate, calendar.get(Calendar.YEAR),
                     calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                     .show()
         }
         categoryViewModel.getAllCategory().observe(this, Observer {
             if(it.isNotEmpty()){
                 val category = ArrayList<String>()
                 it.forEachIndexed { _,element ->
                     category.add(element.categoryAttributes!!.name)
                 }
                 val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, category)
                 category_edittext.threshold = 1
                 category_edittext.setAdapter(adapter)
             }
         })
         currencyViewModel.currencyCode.observe(this, Observer {
             currency = it
         })
         currencyViewModel.currencyDetails.observe(this, Observer {
             currency_edittext.setText(it)
         })
         placeHolderToolbar.setNavigationOnClickListener { unReveal(dialog_add_transaction_layout) }
         currency_edittext.setOnClickListener{
             CurrencyListBottomSheet().show(requireFragmentManager(), "currencyList" )
         }
         tags_chip.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR)
         tags_chip.enableEditChipOnTouch(false, true)
         tagsViewModel.getAllTags().observe(this, Observer {
             it.forEachIndexed{ _, tagsData ->
                 tags.add(tagsData.tagsAttributes?.tag!!)
             }
             val tagsAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, tags)
             tags_chip.threshold = 1
             tags_chip.setAdapter(tagsAdapter)
         })
         currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
             val currencyData = defaultCurrency[0].currencyAttributes
             currency_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
         })
         accountViewModel.isLoading.observe(this, Observer {
             if(it == true){
                 ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
             } else {
                 ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
             }
         })
     }

     private fun contextSwitch(){
         when {
             Objects.equals(transactionType, "Transfer") -> zipLiveData(accountViewModel.getAssetAccounts(), piggyViewModel.getAllPiggyBanks())
                     .observe(this, Observer { transferData ->
                         transferData.first.forEachIndexed { _, accountData ->
                             accounts.add(accountData.accountAttributes?.name!!)
                         }
                         val uniqueAccount = HashSet(accounts).toArray()
                         spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uniqueAccount)
                         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                         source_spinner.isVisible = true
                         source_textview.isVisible = true
                         source_layout.isGone = true
                         source_spinner.adapter = spinnerAdapter
                         destination_layout.isGone = true
                         destination_spinner.isVisible = true
                         destination_textview.isVisible = true
                         destination_spinner.adapter = spinnerAdapter
                         if(transactionId == 0L) {
                             transferData.second.forEachIndexed { _, piggyData ->
                                 piggyBankList.add(piggyData.piggyAttributes?.name!!)
                             }
                             piggy_layout.isVisible = true
                             val uniquePiggy = HashSet(piggyBankList).toArray()
                             val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, uniquePiggy)
                             piggy_edittext.threshold = 1
                             piggy_edittext.setAdapter(adapter)
                         }
                         val sourcePosition = spinnerAdapter.getPosition(sourceName)
                         source_spinner.setSelection(sourcePosition)
                         val destinationPosition = spinnerAdapter.getPosition(destinationName)
                         destination_spinner.setSelection(destinationPosition)
                     })
             Objects.equals(transactionType, "Deposit") -> zipLiveData(accountViewModel.getRevenueAccounts(), accountViewModel.getAssetAccounts())
                     .observe(this , Observer {
                         // Revenue account, autocomplete
                         it.first.forEachIndexed { _, accountData ->
                             sourceAccounts.add(accountData.accountAttributes?.name!!)
                         }
                         val uniqueSource = HashSet(sourceAccounts).toArray()
                         // Asset account, spinner
                         it.second.forEachIndexed { _, accountData ->
                             destinationAccounts.add(accountData.accountAttributes?.name!!)
                         }
                         val uniqueDestination = HashSet(destinationAccounts).toArray()
                         spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uniqueDestination)
                         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                         destination_spinner.adapter = spinnerAdapter
                         destination_textview.isVisible = true
                         destination_layout.isVisible = false
                         val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, uniqueSource)
                         source_edittext.threshold = 1
                         source_edittext.setAdapter(autocompleteAdapter)
                         source_spinner.isVisible = false
                         source_textview.isVisible = false
                         val destinationPosition = spinnerAdapter.getPosition(destinationName)
                         destination_spinner.setSelection(destinationPosition)
                     })
             else -> {
                 zipLiveData(accountViewModel.getAssetAccounts(), accountViewModel.getExpenseAccounts())
                         .observe(this, Observer {
                             // Spinner for source account
                             it.first.forEachIndexed { _, accountData ->
                                 sourceAccounts.add(accountData.accountAttributes?.name!!)
                             }
                             val uniqueSource = HashSet(sourceAccounts).toArray()
                             spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uniqueSource)
                             spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                             source_layout.isVisible = false
                             source_spinner.adapter = spinnerAdapter
                             source_textview.isVisible = true
                             // This is used for auto complete for destination account
                             it.second.forEachIndexed { _, accountData ->
                                 destinationAccounts.add(accountData.accountAttributes?.name!!)
                             }
                             val uniqueDestination = HashSet(destinationAccounts).toArray()
                             val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, uniqueDestination)
                             destination_edittext.threshold = 1
                             destination_edittext.setAdapter(autocompleteAdapter)
                             destination_spinner.isVisible = false
                             destination_textview.isVisible = false
                             val spinnerPosition = spinnerAdapter.getPosition(sourceName)
                             source_spinner.setSelection(spinnerPosition)
                             val destinationPosition = spinnerAdapter.getPosition(destinationName)
                             destination_spinner.setSelection(destinationPosition)
                         })
                 billViewModel.getAllBills().observe(this, Observer {
                     if(it.isNotEmpty()){
                         it.forEachIndexed { _,billData ->
                             bill.add(billData.billAttributes?.name!!)
                         }
                         bill_layout.isVisible = true
                         val uniqueBill = HashSet(bill).toArray()
                         val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, uniqueBill)
                         bill_edittext.threshold = 1
                         bill_edittext.setAdapter(adapter)
                     }
                 })
             }
         }
     }

    private fun submitData(){
        transactionViewModel.addTransaction(transactionType, description_edittext.getString(),
                transaction_date_edittext.getString(), piggyBank, billName,
                transaction_amount_edittext.getString(), sourceAccount, destinationAccount,
                currency, categoryName, transactionTags).observe(this, Observer { transactionResponse ->
            ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
            val errorMessage = transactionResponse.getErrorMessage()
            if (transactionResponse.getResponse() != null) {
                toastSuccess("Transaction Added")
                dialog?.dismiss()
            } else if(errorMessage != null){
                toastError(errorMessage)
            } else if(transactionResponse.getError() != null){
                when {
                    Objects.equals("transfers", transactionType) -> {
                        val transferBroadcast = Intent(requireContext(), TransactionReceiver::class.java).apply {
                            action = "firefly.hisname.ADD_TRANSFER"
                        }
                        val extras = bundleOf(
                                "description" to description_edittext.getString(),
                                "date" to transaction_date_edittext.getString(),
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
                                "date" to transaction_date_edittext.getString(),
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
                                "date" to transaction_date_edittext.getString(),
                                "amount" to transaction_amount_edittext.getString(),
                                "currency" to currency,
                                "sourceName" to sourceAccount,
                                "billName" to billName,
                                "category" to categoryName,
                                "tags" to transactionTags
                        )
                        withdrawalBroadcast.putExtras(extras)
                        requireActivity().sendBroadcast(withdrawalBroadcast)
                    }
                }
                toastOffline(getString(R.string.data_added_when_user_online, transactionType))
                dialog?.dismiss()
            }
        })
    }

    private fun updateData(){
        transactionViewModel.updateTransaction(transactionId,transactionType, description_edittext.getString(),
                transaction_date_edittext.getString(), billName,
                transaction_amount_edittext.getString(), sourceAccount, destinationAccount,
                currency, categoryName, transactionTags).observe(this, Observer { transactionResponse->
            ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
            val errorMessage = transactionResponse.getErrorMessage()
            if (transactionResponse.getResponse() != null) {
                toastSuccess("Transaction Updated")
                dialog?.dismiss()
            } else if(errorMessage != null) {
                toastError(errorMessage)
            } else if(transactionResponse.getError() != null) {
                toastError(transactionResponse.getError()?.localizedMessage)
            }
        })
    }

}