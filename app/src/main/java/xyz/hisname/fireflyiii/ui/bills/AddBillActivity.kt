package xyz.hisname.fireflyiii.ui.bills

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_add_bill.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.ErrorModel
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.BillsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import java.util.*

class AddBillActivity: AppCompatActivity() {

    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val baseUrl: String by lazy { sharedPref.getString("fireflyUrl", "") }
    private val accessToken: String by lazy { sharedPref.getString("access_token","") }
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
                bill_date.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }

        bill_date.setOnClickListener {
            DatePickerDialog(this, startDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }


    }

    private fun validateInput(): Boolean{
        var shouldContinue = true

        if(amount_max_edittext.text.toString().isBlank()){
            amount_max_layout.error = "Please enter a value"
            shouldContinue = false
        }
        if(amount_min_edittext.text.toString().isBlank()){
            amount_min_layout.error = "Please enter a value"
            shouldContinue = false
        }
        if(shouldContinue) {
            if (amount_max_edittext.text.toString().toInt() < amount_min_edittext.text.toString().toInt()) {
                amount_max_layout.error = "Max amount should be more than min amount"
                amount_min_layout.error = "Min amount should be less than max amount"
                shouldContinue = false
            }
        }
        if(!amount_max_edittext.text.toString().isDigitsOnly()){
            amount_max_layout.error = "Please enter numbers only"
            shouldContinue = false
        }
        if(!amount_min_edittext.text.toString().isDigitsOnly()){
            amount_min_layout.error = "Please enter numbers only"
            shouldContinue = false
        }
        if(bill_name_edittext.text.toString().isBlank() || bill_name_edittext.text.toString().isEmpty()){
            bill_name_layout.error = "Please enter a name"
            shouldContinue = false
        }
        if(amount_min_edittext.text.toString().isBlank() || amount_min_edittext.text.toString().isEmpty()) {
            amount_min_layout.error = "Please enter a value"
            shouldContinue = false
        }
        if(amount_max_edittext.text.toString().isBlank() || amount_max_edittext.text.toString().isEmpty()){
            amount_max_layout.error = "Please enter a value"
            shouldContinue = false
        }

        if(bill_date.text.toString().isBlank() || bill_date.text.toString().isEmpty()){
            bill_date_layout.error = "Please select a date"
            shouldContinue = false
        }
        if(!skip_edittext.text.toString().isDigitsOnly()){
            skip_layout.error = "Please enter numbers only"
            shouldContinue = false
        }

        if(skip_edittext.text.toString().isBlank() || skip_edittext.text.toString().isEmpty()){
            skip_layout.error = "Please enter a value"
            shouldContinue = false
        }
        if(currency_code_edittext.text.toString().isBlank() || currency_code_edittext.text.toString().isEmpty()){
            currency_code_layout.error = "Please enter a value"
            shouldContinue = false
        }
        return shouldContinue
    }

    private fun checkEmptiness(){
        if(skip_edittext.text.toString().isBlank() && bill_date.text.toString().isBlank() &&
                note_edittext.text.toString().isBlank() && amount_max_edittext.text.toString().isBlank() &&
                amount_min_edittext.text.toString().isBlank() && bill_name_edittext.text.toString().isBlank()
        && bill_match_edittext.text.toString().isBlank() && currency_code_edittext.text.toString().isBlank()){
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
                val notes: String? = if(note_edittext.text.toString().isBlank()){
                    null
                } else {
                    note_edittext.text.toString()
                }
                val repeatFreq = repeat_spinner.selectedItem.toString().substring(0,1).toLowerCase() +
                        repeat_spinner.selectedItem.toString().substring(1)
                model.addBill(baseUrl,accessToken,bill_name_edittext.text.toString(),
                        bill_match_edittext.text.toString(),amount_min_edittext.text.toString(),
                        amount_max_edittext.text.toString(), bill_date.text.toString(), repeatFreq,
                        skip_edittext.text.toString(), "1", "1",
                        currency_code_edittext.text.toString(), notes)
                        .observe(this, Observer {
                            if(it.getErrorMessage() != null) {
                                val errorMessage = it.getErrorMessage()
                                val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                                // This error parsing is sick...
                                ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                                when {
                                    gson.errors.name != null -> toastError(gson.errors.name[0])
                                    gson.errors.currency_code != null -> toastError(gson.errors.currency_code[0])
                                    else -> toastError("Error occurred while saving bill")
                                }
                            } else if(it.getError() != null){
                                ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
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