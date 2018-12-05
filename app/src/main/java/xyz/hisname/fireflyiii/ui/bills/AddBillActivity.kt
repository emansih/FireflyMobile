package xyz.hisname.fireflyiii.ui.bills

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color.rgb
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_add_bill.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.receiver.BillReceiver
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddBillActivity: BaseActivity() {

    private val model: BillsViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private var billAttribute: BillAttributes? = null
    private var notes: String? = null
    private var repeatFreq: String = ""
    private lateinit var currency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)
        setSupportActionBar(placeHolderToolbar)
        setWidgets()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        setIcons()
        placeHolderToolbar.setNavigationOnClickListener {
            checkEmptiness()
        }
        addBill()
        /*
         if(intent.getStringExtra("status") != null) {
            billAttribute = Gson().fromJson(intent.getSerializableExtra("billData").toString(),
                    BillAttributes::class.java)
        }
         */
    }

    private fun setIcons(){
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(this).icon(FontAwesome.Icon.faw_money_bill)
                        .color(ContextCompat.getColor(this, R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        min_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(this).icon(FontAwesome.Icon.faw_dollar_sign)
                        .color(ContextCompat.getColor(this, R.color.md_yellow_A700))
                        .sizeDp(16),null, null, null)
        max_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(this).icon(FontAwesome.Icon.faw_dollar_sign)
                        .color(ContextCompat.getColor(this, R.color.md_yellow_A700))
                        .sizeDp(16),null, null, null)
        bill_date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_calendar)
                .color(ColorStateList.valueOf(rgb(18, 122, 190)))
                .sizeDp(24),null, null, null)
        rules_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(this).icon(FontAwesome.Icon.faw_ruler)
                        .color(ContextCompat.getColor(this, R.color.md_brown_500))
                        .sizeDp(24),null, null, null)
        skip_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(this).icon(FontAwesome.Icon.faw_sort_numeric_up)
                        .color(ContextCompat.getColor(this, R.color.md_black_1000))
                        .sizeDp(24),null, null, null)
        addBillFab.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_dark))
        addBillFab.setImageDrawable(IconicsDrawable(this).icon(FontAwesome.Icon.faw_save)
                .color(ContextCompat.getColor(this, R.color.md_black_1000))
                .sizeDp(24))
    }

    private fun setWidgets(){
        val spinnerValue = arrayOf(Constants.WEEKLY, Constants.MONTHLY,
                Constants.QUARTERLY, Constants.HALF_YEARLY, Constants.YEARLY)
        val dataAdapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinnerValue)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frequency_spinner.adapter = dataAdapter
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
            hideKeyboard()
            DatePickerDialog(this, startDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        currency_edittext.setOnClickListener{
            hideKeyboard()
            val currencyListFragment = CurrencyListFragment()
            currencyListFragment.show(supportFragmentManager, "currencyList" )
        }
        currencyViewModel.currencyCode.observe(this, Observer {
            currency = it
        })

        currencyViewModel.currencyDetails.observe(this, Observer {
            currency_edittext.setText(it)
        })
    }

    private fun validateInput(): Boolean{
        if(min_amount_edittext.isBlank()){
            min_amount_layout.error = resources.getString(R.string.required_field)
            return false
        }
        if(max_amount_edittext.isBlank()){
            max_amount_layout.error = resources.getString(R.string.required_field)
            return false
        }
        if (max_amount_edittext.getDigits() < min_amount_edittext.getDigits()) {
            max_amount_layout.error = "Max amount should be more than min amount"
            min_amount_layout.error = "Min amount should be less than max amount"
            return false
        }
        if(description_edittext.isBlank()){
            description_edittext.error = resources.getString(R.string.required_field)
            return false
        }
        if(min_amount_edittext.isBlank()) {
            min_amount_edittext.error = resources.getString(R.string.required_field)
            return false
        }
        if(max_amount_edittext.isBlank()){
            max_amount_layout.error = resources.getString(R.string.required_field)
            return false
        }

        if(bill_date_edittext.isBlank()){
            bill_date_layout.error = resources.getString(R.string.required_field)
            return false
        }
        if(!skip_edittext.isDigitsOnly()){
            skip_layout.error = resources.getString(R.string.required_field)
            return false
        }
        if(skip_edittext.isBlank()){
            skip_layout.error = resources.getString(R.string.required_field)
            return false
        }
        if(currency_edittext.isBlank()){
            currency_layout.error = resources.getString(R.string.required_field)
            return false
        }
        return true
    }

    private fun checkEmptiness(){
        if(skip_edittext.isBlank() && bill_date_edittext.isBlank() &&
                notes_edittext.isBlank() && max_amount_edittext.isBlank() &&
                min_amount_edittext.isBlank() && description_edittext.isBlank()
                && rules_edittext.isBlank() && currency_edittext.isBlank()){
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

    private fun addBill(){
        addBillFab.setOnClickListener {
            hideKeyboard()
            notes = if(notes_edittext.isBlank()){
                null
            } else {
                notes_edittext.getString()
            }
            repeatFreq = frequency_spinner.selectedItem.toString().substring(0,1).toLowerCase() +
                    frequency_spinner.selectedItem.toString().substring(1)
            if(validateInput()) {
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                model.addBill(description_edittext.getString(),
                        rules_edittext.getString(), min_amount_edittext.getString(),
                        max_amount_edittext.getString(), bill_date_edittext.getString(), repeatFreq,
                        skip_edittext.getString(), "1", "1",
                        currency, notes)
                        .observe(this, Observer { response ->
                            ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                            val errorMessage = response.getErrorMessage()
                            if (errorMessage != null) {
                                toastError(errorMessage)
                            } else if (response.getError() != null) {
                                val billBroadcast = Intent(this, BillReceiver::class.java).apply {
                                    action = "firefly.hisname.ADD_BILL"
                                }
                                val extras = bundleOf(
                                        "name" to description_edittext.getString(),
                                        "billMatch" to rules_edittext.getString(),
                                        "minAmount" to min_amount_edittext.getString(),
                                        "maxAmount" to max_amount_edittext.getString(),
                                        "billDate" to bill_date_edittext.getString(),
                                        "repeatFreq" to repeatFreq,
                                        "skip" to skip_edittext.getString(),
                                        "currencyCode" to currency,
                                        "notes" to notes)
                                billBroadcast.putExtras(extras)
                                sendBroadcast(billBroadcast)
                                toastOffline(getString(R.string.data_added_when_user_online, "Bill"))
                                finish()
                            } else if (response.getResponse() != null) {
                                toastSuccess("Bill saved")
                                finish()
                            }

                        })
            } else {
                toastInfo("Please check for errors")
            }
        }
    }

    override fun onBackPressed() {
        checkEmptiness()
    }
}