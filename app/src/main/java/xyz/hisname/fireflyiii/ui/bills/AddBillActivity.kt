package xyz.hisname.fireflyiii.ui.bills

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_bill.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.ErrorModel
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.BillsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddBillActivity: BaseActivity() {

    private val model: BillsViewModel by lazy { getViewModel(BillsViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)
        setupWidgets()
    }

    private fun setupWidgets(){
        setSupportActionBar(bill_create_toolbar)
        bill_create_toolbar.setNavigationOnClickListener {
            checkEmptiness()
        }
        val spinnerValue = ArrayList<String>()
        spinnerValue.add(Constants.WEEKLY)
        spinnerValue.add(Constants.MONTHLY)
        spinnerValue.add(Constants.QUARTERLY)
        spinnerValue.add(Constants.HALF_YEARLY)
        spinnerValue.add(Constants.YEARLY)
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
        if(!amount_max_edittext.isDigitsOnly()){
            amount_max_edittext.error = "Please enter numbers only"
            shouldContinue = false
        }
        if(!amount_min_edittext.isDigitsOnly()){
            amount_min_edittext.error = "Please enter numbers only"
            shouldContinue = false
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_item_save){
            if(validateInput()){
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                val notes: String? = if(note_edittext.isBlank()){
                    null
                } else {
                    note_edittext.getString()
                }
                val repeatFreq = repeat_spinner.selectedItem.toString().substring(0,1).toLowerCase() +
                        repeat_spinner.selectedItem.toString().substring(1)
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
                                // This error parsing is sick...
                                when {
                                    gson.errors.name != null -> toastError(gson.errors.name[0])
                                    gson.errors.currency_code != null -> toastError(gson.errors.currency_code[0])
                                    else -> toastError("Error occurred while saving bill")
                                }
                            } else if(it.getError() != null){
                                if (it.getError()!!.localizedMessage.startsWith("Unable to resolve host")) {
                                    toastError(resources.getString(R.string.unable_ping_server))
                                } else {
                                    // yea this is weird
                                    toastSuccess("Bill saved")
                                    finish()
                                }
                            } else {
                                toastSuccess("Bill saved")
                                finish()
                            }

                        })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        checkEmptiness()
    }
}