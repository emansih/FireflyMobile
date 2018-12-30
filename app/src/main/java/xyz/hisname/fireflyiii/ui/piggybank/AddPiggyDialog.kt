package xyz.hisname.fireflyiii.ui.piggybank

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
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.dialog_add_piggy.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.receiver.PiggyBankReceiver
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddPiggyDialog: BaseDialog() {

    private var accounts = ArrayList<String>()
    private var piggyId: Long = 0
    private lateinit var accountAdapter: ArrayAdapter<Any>
    private var currentAmount: String? = null
    private var startDate: String? = null
    private var targetDate: String? = null
    private var notes: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_add_piggy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_piggy_layout)
        piggyId = arguments?.getLong("piggyId") ?: 0
        if(piggyId != 0L){
            piggyViewModel.getPiggyById(piggyId).observe(this, Observer { piggyData ->
                val piggyAttributes = piggyData[0].piggyAttributes
                description_edittext.setText(piggyAttributes?.name)
                target_amount_edittext.setText(piggyAttributes?.target_amount.toString())
                current_amount_edittext.setText(piggyAttributes?.current_amount.toString())
                date_started_edittext.setText(piggyAttributes?.start_date)
                date_target_edittext.setText(piggyAttributes?.target_date)
                note_edittext.setText(piggyAttributes?.notes)
                accountViewModel.getAccountById(piggyAttributes?.account_id!!).observe(this, Observer { accountData ->
                    val accountName = accountData[0].accountAttributes?.name
                    val spinnerPosition = accountAdapter.getPosition(accountName)
                    account_spinner.setSelection(spinnerPosition)
                })
            })
        }
    }

    override fun onStart() {
        super.onStart()
        addPiggyFab.setOnClickListener {
            hideKeyboard()
            ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
            currentAmount = if (current_amount_edittext.isBlank()) {
                null
            } else {
                current_amount_edittext.getString()
            }
            startDate = if (date_started_edittext.isBlank()) {
                null
            } else {
                date_started_edittext.getString()
            }
            targetDate = if (date_target_edittext.isBlank()) {
                null
            } else {
                date_target_edittext.getString()
            }
            notes = if (note_edittext.isBlank()) {
                null
            } else {
                note_edittext.getString()
            }
            if(piggyId == 0L) {
                submitData()
            } else {
                updatePiggyBank()
            }
        }
        placeHolderToolbar.setOnClickListener {
            unReveal(dialog_add_piggy_layout)
        }
    }

    override fun setIcons(){
        target_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        current_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        date_started_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_calendar)
                .color(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
                .sizeDp(24),null, null, null)
        date_target_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_calendar)
                .color(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
                .sizeDp(24),null, null, null)
        addPiggyFab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
        addPiggyFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_save)
                .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                .sizeDp(24))
        placeHolderToolbar.navigationIcon = navIcon
    }

    override fun setWidgets(){
        val calendar = Calendar.getInstance()
        val startDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                date_target_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }

        date_target_edittext.setOnClickListener {
            DatePickerDialog(requireContext(), startDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        val endDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                date_started_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }
        date_started_edittext.setOnClickListener {
            DatePickerDialog(requireContext(), endDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        accountViewModel.getAssetAccounts().observe(this, Observer {
            if(it.isNotEmpty()) {
                it.forEachIndexed { _, accountData ->
                    accounts.add(accountData.accountAttributes?.name!!)
                }
                val uniqueAccount = HashSet(accounts).toArray()
                accountAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uniqueAccount)
                accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                account_spinner.adapter = accountAdapter
            }
        })
    }

    override fun submitData(){
        accountViewModel.getAccountByName(account_spinner.selectedItem.toString()).observe(this, Observer { accountData ->
            piggyViewModel.addPiggyBank(description_edittext.getString(), accountData[0].accountId.toString(),
                    currentAmount, notes, startDate, target_amount_edittext.getString(), targetDate)
                    .observe(this, Observer {
                        ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
                        val errorMessage = it.getErrorMessage()
                        if (errorMessage != null) {
                            toastError(errorMessage)
                        } else if (it.getError() != null) {
                            if (it.getError()!!.localizedMessage.startsWith("Unable to resolve host")) {
                                val piggyBroadcast = Intent(requireContext(), PiggyBankReceiver::class.java).apply {
                                    action = "firefly.hisname.ADD_PIGGY_BANK"
                                }
                                val extras = bundleOf(
                                        "name" to description_edittext.getString(),
                                        "accountId" to accountData[0].accountId.toString(),
                                        "targetAmount" to target_amount_edittext.getString(),
                                        "currentAmount" to currentAmount,
                                        "startDate" to startDate,
                                        "endDate" to targetDate,
                                        "notes" to notes
                                )
                                piggyBroadcast.putExtras(extras)
                                requireActivity().sendBroadcast(piggyBroadcast)
                                toastOffline(getString(R.string.data_added_when_user_online, "Piggy Bank"))
                                dialog?.dismiss()
                            } else {
                                toastError("Error saving piggy bank")
                            }
                        } else if (it.getResponse() != null) {
                            toastSuccess("Piggy bank saved")
                            dialog?.dismiss()
                        }
                    })
        })
    }

    private fun updatePiggyBank(){
        accountViewModel.getAccountByName(account_spinner.selectedItem.toString()).observe(this, Observer { accountData ->
            piggyViewModel.updatePiggyBank(piggyId, description_edittext.getString(), accountData[0].accountId.toString(),
                    currentAmount, notes, startDate, target_amount_edittext.getString(), targetDate).observe(this, Observer {
                ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
                if (it.getErrorMessage() != null) {
                    toastError(it.getErrorMessage())
                } else if (it.getError() != null) {
                    toastError(it.getError()?.localizedMessage)
                } else if (it.getResponse() != null) {
                    toastSuccess("Piggy bank updated")
                    requireFragmentManager().popBackStack()
                    val bundle = bundleOf("piggyId" to piggyId)
                    requireFragmentManager().commit {
                        replace(R.id.fragment_container, PiggyDetailFragment().apply { arguments = bundle })
                        addToBackStack(null)
                    }
                    dialog?.dismiss()
                }
            })
        })
    }

}