package xyz.hisname.fireflyiii.ui.transaction

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
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
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddTransactionDialog: DialogFragment() {

    private val categoryViewModel by lazy { getViewModel(CategoryViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private val billViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private val piggyViewModel by lazy { getViewModel(PiggyViewModel::class.java) }
    private val transactionViewModel by lazy { getViewModel(TransactionsViewModel::class.java) }
    private lateinit var currency: String
    private var accounts = ArrayList<String>()
    private var sourceAccounts = ArrayList<String>()
    private var destinationAccounts = ArrayList<String>()
    private var piggyBank = ArrayList<String>()
    private val bill = ArrayList<String>()
    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_add_transaction, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setIcons()
        setWidgets()
        contextSwitch()
        addTransactionFab.setOnClickListener {
            submitData()
        }
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
         source_edittext.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(),
                 R.drawable.ic_bank_transfer),null, null, null)
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
        placeHolderToolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.abc_ic_clear_material)
        addTransactionFab.setImageDrawable(IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_add)
                .color(ContextCompat.getColor(requireContext(), R.color.md_pink_200))
                .sizeDp(16))
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
         placeHolderToolbar.setNavigationOnClickListener { dialog?.dismiss() }
         currency_edittext.setOnClickListener{
             CurrencyListFragment().show(requireFragmentManager(), "currencyList" )
         }
     }

     private fun contextSwitch(){
         when {
             Objects.equals(transactionType, "Transfer") -> zipLiveData(accountViewModel.getAssetAccounts(), piggyViewModel.getAllPiggyBanks())
                     .observe(this, Observer {
                         it.first.forEachIndexed { _, accountData ->
                             accounts.add(accountData.accountAttributes?.name!!)
                         }
                         val uniqueValues = HashSet(accounts).toArray()
                         val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uniqueValues)
                         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                         source_spinner.isVisible = true
                         source_textview.isVisible = true
                         source_layout.isGone = true
                         source_spinner.adapter = spinnerAdapter
                         destination_layout.isGone = true
                         destination_spinner.isVisible = true
                         destination_textview.isVisible = true
                         destination_spinner.adapter = spinnerAdapter
                         it.second.forEachIndexed { _,piggyData ->
                             piggyBank.add(piggyData.piggyAttributes?.name!!)
                         }
                         val uniquePiggy = HashSet(piggyBank).toArray()
                         val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, uniquePiggy)
                         piggy_edittext.threshold = 1
                         piggy_edittext.setAdapter(adapter)
                     })
             Objects.equals(transactionType, "Deposit") -> zipLiveData(accountViewModel.getRevenueAccounts(), accountViewModel.getAssetAccounts())
                     .observe(this , Observer {
                         // Revenue account, autocomplete
                         it.first.forEachIndexed { _, accountData ->
                             sourceAccounts.add(accountData.accountAttributes?.name!!)
                         }
                         // Asset account, spinner
                         it.second.forEachIndexed { _, accountData ->
                             destinationAccounts.add(accountData.accountAttributes?.name!!)
                         }
                         val uniqueSource = HashSet(sourceAccounts).toArray()
                         val uniqueDestination = HashSet(destinationAccounts).toArray()
                         val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uniqueDestination)
                         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                         destination_spinner.adapter = spinnerAdapter
                         destination_textview.isVisible = true
                         destination_layout.isVisible = false
                         val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, uniqueSource)
                         source_edittext.threshold = 1
                         source_edittext.setAdapter(autocompleteAdapter)
                         source_spinner.isVisible = false
                         source_textview.isVisible = false
                     })
             else -> {
                 zipLiveData(accountViewModel.getAssetAccounts(), accountViewModel.getExpenseAccounts())
                         .observe(this, Observer {
                             // Spinner for source account
                             it.first.forEachIndexed { _, accountData ->
                                 sourceAccounts.add(accountData.accountAttributes?.name!!)
                             }
                             val uniqueSource = HashSet(sourceAccounts).toArray()
                             val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uniqueSource)
                             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                             source_layout.isVisible = false
                             source_spinner.adapter = adapter
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
                         })
                 billViewModel.getAllBills().observe(this, Observer {
                     if(it.isNotEmpty()){
                         it.forEachIndexed { _,billData ->
                             bill.add(billData.billAttributes?.name!!)
                         }
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
        ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
        hideKeyboard()
        val billName: String? = if(bill_edittext.isBlank()){
            null
        } else {
            bill_edittext.getString()
        }
        val piggyBank: String? = if(piggy_edittext.isBlank()){
            null
        } else {
            piggy_edittext.getString()
        }
        val categoryName: String? = if(category_edittext.isBlank()){
            null
        } else {
            category_edittext.getString()
        }
        var sourceAccount = ""
        var destinationAccount = ""
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
        transactionViewModel.addTransaction(transactionType, description_edittext.getString(),
                transaction_date_edittext.getString(), piggyBank,
                billName, transaction_amount_edittext.getString(), sourceAccount,
                destinationAccount, currency, categoryName
        ).observe(this, Observer { transactionResponse ->
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
                                "piggyBankName" to piggyBank,
                                "category" to categoryName
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
                                "category" to categoryName
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
                                "category" to categoryName
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

}