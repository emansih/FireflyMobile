package xyz.hisname.fireflyiii.ui.piggybank

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
import kotlinx.android.synthetic.main.activity_piggy_create.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.receiver.PiggyBankReceiver
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.repository.piggybank.PiggyViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddPiggyActivity: BaseActivity(){

    private val model by lazy { getViewModel(PiggyViewModel::class.java) }
    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private var accounts = ArrayList<String>()

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
        accountViewModel.getAssetAccounts().observe(this, Observer {
            if(it.isNotEmpty()) {
                it.forEachIndexed { _, accountData ->
                    accounts.add(accountData.accountAttributes?.name!!)
                }
                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, accounts)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                account_id_edittext.adapter = adapter
            }
        })
        piggy_name_edittext.setText(intent.getStringExtra("piggyName"))
        val targetAmount = intent.getSerializableExtra("targetAmount") ?: ""
        target_amount_edittext.setText(targetAmount.toString())
        val currentAmount = intent.getSerializableExtra("currentAmount") ?: ""
        current_amount_edittext.setText(currentAmount.toString())
        start_date_edittext.setText(intent.getStringExtra("startDate") ?: DateTimeUtil.getTodayDate())
        target_date_edittext.setText(intent.getStringExtra("targetDate") ?: "")
        note_edittext.setText(intent.getStringExtra("notes"))
    }

    private fun validateInput(): Boolean {
        var shouldContinue = true
        if(piggy_name_edittext.isBlank()){
            piggy_name_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        if(target_amount_edittext.isBlank()){
            target_amount_edittext.error = resources.getString(R.string.required_field)
            shouldContinue = false
        }
        return shouldContinue
    }

    private fun checkEmptiness(){
        if(piggy_name_edittext.isBlank() and
                target_amount_edittext.isBlank() and current_amount_edittext.isBlank() and
                start_date_edittext.isBlank() and target_date_edittext.isBlank() and note_edittext.isBlank()){
            toastInfo("No information entered. Piggy bank not saved")
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
            hideKeyboard()
            if (validateInput()) {
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                val currentAmount: String? = if (current_amount_edittext.isBlank()) {
                    null
                } else {
                    current_amount_edittext.getString()
                }
                val startDate = if (start_date_edittext.isBlank()) {
                    null
                } else {
                    start_date_edittext.getString()
                }
                val targetDate = if (target_date_edittext.isBlank()) {
                    null
                } else {
                    target_date_edittext.getString()
                }
                val notes = if (note_edittext.isBlank()) {
                    null
                } else {
                    note_edittext.getString()
                }
                accountViewModel.getAccountByName(account_id_edittext.selectedItem.toString()).observe(this, Observer { accountData ->
                    model.addPiggyBank(piggy_name_edittext.getString(), accountData[0].accountId.toString(),
                            currentAmount, notes, startDate, target_amount_edittext.getString(), targetDate)
                            .observe(this, Observer {
                                ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                                val errorMessage = it.getErrorMessage()
                                if (errorMessage != null) {
                                    toastError(errorMessage)
                                } else if (it.getError() != null) {
                                    if (it.getError()!!.localizedMessage.startsWith("Unable to resolve host")) {
                                        val piggyBroadcast = Intent(this, PiggyBankReceiver::class.java).apply {
                                            action = "firefly.hisname.ADD_PIGGY_BANK"
                                        }
                                        val extras = bundleOf(
                                                "name" to piggy_name_edittext.getString(),
                                                "accountId" to accountData[0].accountId.toString(),
                                                "targetAmount" to target_amount_edittext.getString(),
                                                "currentAmount" to currentAmount,
                                                "startDate" to startDate,
                                                "endDate" to targetDate,
                                                "notes" to notes
                                        )
                                        piggyBroadcast.putExtras(extras)
                                        sendBroadcast(piggyBroadcast)
                                        toastOffline(getString(R.string.data_added_when_user_online, "Piggy Bank"))
                                        finish()
                                    } else {
                                        toastError("Error saving piggy bank")
                                    }
                                } else if (it.getResponse() != null) {
                                    toastSuccess("Piggy bank saved")
                                    finish()
                                }
                            })
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