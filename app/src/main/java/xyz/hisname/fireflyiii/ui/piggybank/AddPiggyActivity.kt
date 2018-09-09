package xyz.hisname.fireflyiii.ui.piggybank

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_piggy_create.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.PiggyViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddPiggyActivity: BaseActivity() {

    private val model: PiggyViewModel by lazy { getViewModel(PiggyViewModel::class.java) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piggy_create)
        setSupportActionBar(piggy_create_toolbar)
        piggy_create_toolbar.setNavigationOnClickListener{ checkEmptiness() }
        setWidgets()
    }

    private fun setWidgets(){
        val calendar = Calendar.getInstance()
        val startDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                target_date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }

        target_date_edittext.setOnClickListener {
            DatePickerDialog(this, startDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        val endDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                start_date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }
        start_date_edittext.setOnClickListener {
            DatePickerDialog(this, endDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
    }

    private fun validateInput(): Boolean {
        var shouldContinue = true
        if(piggy_name_edittext.isBlank()){
            piggy_name_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(account_id_edittext.isBlank()){
            account_id_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(target_amount_edittext.isBlank()){
            target_amount_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(shouldContinue){
            if(!target_amount_edittext.isDigitsOnly()){
                target_amount_edittext.error = "Please enter numbers only"
                shouldContinue = false
            }
        }
        if(!current_amount_edittext.isBlank() && !current_amount_edittext.isDigitsOnly()){
            current_amount_edittext.error = "Please enter numbers only"
            shouldContinue = false
        }
        return shouldContinue
    }

    private fun checkEmptiness(){
        if(piggy_name_edittext.isBlank() and account_id_edittext.isBlank() and
                target_amount_edittext.isBlank() and current_amount_edittext.isBlank() and
                start_date_edittext.isBlank() and target_date_edittext.isBlank() and note_edittext.isBlank()){
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_item_save) {
            if (validateInput()) {
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                val currentAmount = if (current_amount_edittext.isBlank()) {
                    ""
                } else {
                    current_amount_edittext.getString()
                }
                val startDate = if (start_date_edittext.isBlank()) {
                    ""
                } else {
                    start_date_edittext.getString()
                }
                val targetDate = if (target_date_edittext.isBlank()) {
                    ""
                } else {
                    target_date_edittext.getString()
                }
                val notes = if (note_edittext.isBlank()) {
                    ""
                } else {
                    note_edittext.getString()
                }
                model.addPiggyBank(baseUrl, accessToken, piggy_name_edittext.getString(), account_id_edittext.getString(),
                        currentAmount, notes, startDate, target_amount_edittext.getString(), targetDate)
                        .observe(this, Observer {
                            ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)


                        })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        checkEmptiness()
    }

}