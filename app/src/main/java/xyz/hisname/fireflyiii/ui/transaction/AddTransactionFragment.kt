package xyz.hisname.fireflyiii.ui.transaction

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.ErrorModel
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.TransactionViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*



class AddTransactionFragment: BaseFragment() {

    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupWidgets()
    }

    private fun setupWidgets(){
        transactionDateEditText.setText(DateTimeUtil.getTodayDate())
        val calendar = Calendar.getInstance()
        val transactionDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                transactionDateEditText.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }
        transactionDateEditText.setOnClickListener {
            DatePickerDialog(requireContext(), transactionDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        if(Objects.equals(transactionType, "expenses")){
            billEditText.isVisible = true
        } else if(Objects.equals(transactionType, "transfers")){
            piggyBankName.isVisible = true
        }
        descriptionEditText.isFocusable = true
        descriptionEditText.isFocusableInTouchMode = true
        descriptionEditText.requestFocus()
    }

    // well...:(
    private fun convertString(pleaseLowerCase: Boolean): String{
        var convertedString = ""
        when {
            Objects.equals(transactionType, "expenses") -> convertedString = if(pleaseLowerCase){
                "withdrawal"
            } else {
                "Withdrawal"
            }
            Objects.equals(transactionType, "income") -> convertedString = if(pleaseLowerCase) {
                "deposit"
            } else {
                "Deposit"
            }
            Objects.equals(transactionType, "transfers") -> convertedString = if(pleaseLowerCase) {
                "transfer"
            } else {
                "Transfer"
            }
        }
        return convertedString
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        requireActivity().activity_toolbar.title = "Add " + convertString(false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().activity_toolbar?.title = "Add " + convertString(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.save_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        if(item?.itemId == R.id.menu_item_save){
            val billName: String? = if(billEditText.isBlank()){
                null
            } else {
                billEditText.getString()
            }
            val sourceName: String? = if(sourceEditText.isBlank()){
                null
            } else {
                sourceEditText.getString()
            }
            val destinationName: String? = if(destinationEditText.isBlank()){
                null
            } else {
                destinationEditText.getString()
            }
            val piggyBank: String? = if(piggyBankName.isBlank()){
                null
            } else {
                piggyBankName.getString()
            }
            ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
            model.addTransaction(baseUrl, accessToken, convertString(true),
                    descriptionEditText.getString(), transactionDateEditText.getString(), piggyBank,
                    billName, transactionAmountEditText.getString(), sourceName, destinationName,
                    currencyEditText.getString()).observe(this, Observer {
                if (it.getSuccess() != null) {
                    toastSuccess("Transaction Added")
                    requireFragmentManager().popBackStack()
                } else if(it.getErrorMessage() != null){
                    ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
                    val errorMessage = it.getErrorMessage()
                    val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                    when {
                        gson.errors.transactions_destination_name != null -> {
                            if(gson.errors.transactions_destination_name.contains("is required")){
                                destinationEditText.error = "Destination Account Required"
                            } else {
                                destinationEditText.error = "Invalid Destination Account"
                            }
                        }
                        gson.errors.transactions_currency != null -> {
                            if(gson.errors.transactions_currency.contains("is required")){
                                currencyEditText.error = "Currency Code Required"
                            } else {
                                currencyEditText.error = "Invalid Currency Code"
                            }
                        }
                        gson.errors.transactions_source_name != null  -> {
                            if(gson.errors.transactions_source_name.contains("is required")){
                                sourceEditText.error = "Source Account Required"
                            } else {
                                sourceEditText.error = "Invalid Source Account"
                            }
                        }
                        gson.errors.bill_name != null -> billEditText.error = "Invalid Bill Name"
                        gson.errors.piggy_bank_name != null -> piggyBankName.error = "Invalid Piggy Bank Name"
                        else -> toastError("Error occurred while saving transaction", Toast.LENGTH_LONG)
                    }
                }
            })
        }
        return true
    }


}