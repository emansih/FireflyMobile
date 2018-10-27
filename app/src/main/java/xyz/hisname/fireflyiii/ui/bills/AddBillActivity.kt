package xyz.hisname.fireflyiii.ui.bills

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_bill.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.repository.models.bills.ErrorModel
import xyz.hisname.fireflyiii.repository.viewmodel.BillsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddBillActivity: BaseActivity(), CurrencyListFragment.OnCompleteListener {

    private val model: BillsViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private var billAttribute: BillAttributes? = null
    private var notes: String? = null
    private var repeatFreq: String = ""

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)
        if(intent.getStringExtra("status") != null) {
            billAttribute = Gson().fromJson(intent.getSerializableExtra("billData").toString(),
                    BillAttributes::class.java)
        }
        setupWidgets()
    }

    private fun setupWidgets(){
        setSupportActionBar(bill_create_toolbar)
        bill_create_toolbar.setNavigationOnClickListener {
            checkEmptiness()
        }
        val spinnerValue = arrayOf(Constants.WEEKLY, Constants.MONTHLY,
                Constants.QUARTERLY, Constants.HALF_YEARLY, Constants.YEARLY)
        val dataAdapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinnerValue)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeat_spinner.adapter = dataAdapter
        val calendar = Calendar.getInstance()
        val startDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                bill_date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }

        bill_date_edittext.setOnClickListener {
            DatePickerDialog(this, startDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        if(intent.getStringExtra("status") != null){
            bill_name_edittext.setText(billAttribute?.name)
            amount_min_edittext.setText(billAttribute?.amount_min.toString())
            amount_max_edittext.setText(billAttribute?.amount_max.toString())
            bill_date_edittext.setText(billAttribute?.date.toString())
            val data = billAttribute?.repeat_freq?.substring(0,1)?.toUpperCase() + billAttribute?.repeat_freq?.substring(1)
            val spinnerPosition = dataAdapter.getPosition(data)
            repeat_spinner.setSelection(spinnerPosition, true)
            skip_edittext.setText(billAttribute?.skip.toString())
            currency_code_edittext.setText(billAttribute?.currency_code)
            note_edittext.setText(billAttribute?.markdown)
        }
        currency_code_edittext.setOnClickListener{
            val currencyListFragment = CurrencyListFragment().apply {
                bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
            }
            currencyListFragment.show(supportFragmentManager, "currencyList" )
            currencyListFragment.setCurrencyListener(this)

        }
    }

    private fun validateInput(): Boolean{
        var shouldContinue = true
        if(amount_max_edittext.isBlank()){
            amount_max_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(amount_min_edittext.isBlank()){
            amount_min_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(shouldContinue) {
            if (amount_max_edittext.getDigits() < amount_min_edittext.getDigits()) {
                amount_max_edittext.error = "Max amount should be more than min amount"
                amount_min_edittext.error = "Min amount should be less than max amount"
                shouldContinue = false
            }
        }
        if(bill_name_edittext.isBlank()){
            bill_name_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(amount_min_edittext.isBlank()) {
            amount_min_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(amount_max_edittext.isBlank()){
            amount_max_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }

        if(bill_date_edittext.isBlank()){
            bill_date_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(!skip_edittext.isDigitsOnly()){
            skip_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }

        if(skip_edittext.isBlank()){
            skip_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(currency_code_edittext.isBlank()){
            currency_code_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        return shouldContinue
    }

    private fun checkEmptiness(){
        if(skip_edittext.isBlank() && bill_date_edittext.isBlank() &&
                note_edittext.isBlank() && amount_max_edittext.isBlank() &&
                amount_min_edittext.isBlank() && bill_name_edittext.isBlank()
        && bill_match_edittext.isBlank() && currency_code_edittext.isBlank()){
            toastInfo("No information entered. Bill not saved")
            finish()
        } else {
            AlertDialog.Builder(this)
                    .setTitle("Are you sure?")
                    .setMessage("The information you entered will not be saved.")
                    .setPositiveButton("OK"){ _,_ ->
                        finish()
                    }
                    .setNegativeButton("Cancel"){ _,_ ->

                    }
                    .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun addBill(){
        model.addBill(baseUrl,accessToken,bill_name_edittext.getString(),
                bill_match_edittext.getString(),amount_min_edittext.getString(),
                amount_max_edittext.getString(), bill_date_edittext.getString(), repeatFreq,
                skip_edittext.getString(), "1", "1",
                currency_code_edittext.getString(), notes)
                .observe(this, Observer {
                    ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                    if(it.getErrorMessage() != null) {
                        val errorMessage = it.getErrorMessage()
                        val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                        when {
                            gson.errors.name != null -> toastError(gson.errors.name[0])
                            gson.errors.currency_code != null -> toastError(gson.errors.currency_code[0])
                            gson.errors.amount_min != null -> toastError(gson.errors.amount_min[0])
                            gson.errors.repeat_freq != null -> toastError(gson.errors.repeat_freq[0])
                            gson.errors.automatch != null -> toastError(gson.errors.automatch[0])
                            else -> toastError("Error occurred while saving bill")
                        }
                    } else if(it.getError() != null){
                        if (it.getError()!!.localizedMessage.startsWith("Unable to resolve host")) {
                            val billBroadcast = Intent("firefly.hisname.ADD_BILL")
                            val extras = bundleOf(
                                    "name" to bill_name_edittext.getString(),
                                    "billMatch" to bill_match_edittext.getString(),
                                    "minAmount" to amount_min_edittext.getString(),
                                    "maxAmount" to amount_max_edittext.getString(),
                                    "billDate" to bill_date_edittext.getString(),
                                    "repeatFreq" to repeatFreq,
                                    "skip" to skip_edittext.getString(),
                                    "currencyCode" to currency_code_edittext.getString(),
                                    "notes" to notes)
                            billBroadcast.putExtras(extras)
                            sendBroadcast(billBroadcast)
                            toastOffline(getString(R.string.data_added_when_user_online, "Bill"))
                            finish()
                        } else {
                            toastError("Error saving bill")
                        }
                    } else if(it.getSuccess() != null){
                        toastSuccess("Bill saved")
                        finish()
                    }

                })
    }

    private fun updateBill(){
        model.updateBill(baseUrl,accessToken, intent.getLongExtra("billId", 0).toString(),
                bill_name_edittext.getString(), bill_match_edittext.getString(),amount_min_edittext.getString(),
                amount_max_edittext.getString(), bill_date_edittext.getString(), repeatFreq,
                skip_edittext.getString(), "1", "1",
                currency_code_edittext.getString(), notes).observe(this, Observer {
            ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
            if(it.getErrorMessage() != null) {

            } else {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_item_save){
            hideKeyboard()
            if(validateInput()){
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                notes = if(note_edittext.isBlank()){
                    null
                } else {
                    note_edittext.getString()
                }
                repeatFreq = repeat_spinner.selectedItem.toString().substring(0,1).toLowerCase() +
                        repeat_spinner.selectedItem.toString().substring(1)
                if(intent.getStringExtra("status") != null){
                    updateBill()
                } else {
                    addBill()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        checkEmptiness()
    }

    override fun onCurrencyClickListener(currency: String) {
        currency_code_edittext.setText(currency)
    }
}