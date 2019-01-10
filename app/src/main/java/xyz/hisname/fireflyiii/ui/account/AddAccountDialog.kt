package xyz.hisname.fireflyiii.ui.account

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.work.*
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.dialog_add_account.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseDialog
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.workers.account.AccountWorker
import java.util.*

class AddAccountDialog: BaseDialog() {

    private val accountType: String by lazy { arguments?.getString("accountType") ?: "" }
    private var currency: String = ""
    private val calendar by lazy { Calendar.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_add_account, container)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_account_layout)
    }

    override fun setIcons() {
        currencycode_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        iban_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_format_list_numbered)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_deep_orange_900))
                        .sizeDp(24),null, null, null)
        account_number_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_supervisor_account)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_red_400))
                        .sizeDp(24),null, null, null)
        ccType_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_credit_card)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_blue_600))
                        .sizeDp(24),null, null, null)
        ccPaymentDate_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_perm_contact_calendar)
                        .color(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
                        .sizeDp(24),null, null, null)
        liabilityAmount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_attach_money)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        liabilityStartDate_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_calendar)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_deep_purple_400))
                        .sizeDp(24),null, null, null)
        liabilityInterest_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_not_interested)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_brown_500))
                        .sizeDp(24),null, null, null)
        placeHolderToolbar.navigationIcon = navIcon
        placeHolderToolbar.setNavigationOnClickListener {
            dialog?.dismiss()
        }
        addAccountFab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
        addAccountFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_credit_card)
                .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                .sizeDp(24))
    }

    override fun setWidgets() {
        if(Objects.equals(accountType, "Asset Account")){
            accountRoleSpinner.isVisible = true
            accountTypeSpinner.isVisible = false
        } else if(Objects.equals(accountType,"Liability Account")) {
            liabilityTypeSpinner.isVisible = true
            liabilityAmount_layout.isVisible = true
            liabilityStartDate_layout.isVisible = true
            liabilityInterest_layout.isVisible = true
            interestPeriod.isVisible = true
        }
        accountTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val index = parent.selectedItemPosition
                accountRoleSpinner.isVisible = index == 0
                liabilityTypeSpinner.isVisible = index == 3
                liabilityAmount_layout.isVisible = index == 3
                liabilityStartDate_layout.isVisible = index == 3
                liabilityInterest_layout.isVisible = index == 3
                interestPeriod.isVisible = index == 3
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        accountRoleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val index = parent.selectedItemPosition
                ccType_layout.isVisible = index == 3
                ccPaymentDate_layout.isVisible = index == 3
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        netWorthText.setOnClickListener {
            netWorthCheckbox.performClick()
        }
        currencyViewModel.currencyCode.observe(this, Observer {
            currency = it
        })
        currencyViewModel.currencyDetails.observe(this, Observer {
            currencycode_edittext.setText(it)
        })
        currencycode_edittext.setOnClickListener{
            val currencyListFragment = CurrencyListBottomSheet()
            currencyListFragment.show(requireFragmentManager(), "currencyList" )
        }
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            currencycode_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
            currency = currencyData?.code ?: ""
        })
        ccPaymentDate_edittext.setOnClickListener {
            val date = DatePickerDialog.OnDateSetListener {
                _, year, monthOfYear, dayOfMonth ->
                run {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    ccPaymentDate_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
                }
            }
            ccPaymentDate_edittext.setOnClickListener {
                DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        .show()
            }
        }
        liabilityStartDate_edittext.setOnClickListener {
            val date = DatePickerDialog.OnDateSetListener {
                _, year, monthOfYear, dayOfMonth ->
                run {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    liabilityStartDate_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
                }
            }
            liabilityStartDate_edittext.setOnClickListener {
                DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        .show()
            }
        }
        addAccountFab.setOnClickListener {
            submitData()
        }

    }

    private fun convertString(): String{
        return when {
            Objects.equals(accountType, "Asset Account") -> "asset"
            Objects.equals(accountType, "Expense Account") -> "expense"
            Objects.equals(accountType, "Revenue Account") -> "revenue"
            Objects.equals(accountType, "Liability Account") -> "liability"
            else -> "Account"
        }
    }

    override fun submitData() {
        val networth = if(netWorthCheckbox.isChecked){
            1
        } else {
            0
        }
        val accountType = if(accountTypeSpinner.isVisible){
            when {
                accountTypeSpinner.selectedItemPosition == 0 -> "asset"
                accountTypeSpinner.selectedItemPosition == 1 -> "expense"
                accountTypeSpinner.selectedItemPosition == 2 -> "revenue "
                else -> "liability"
            }
        } else {
            convertString()
        }

        val accountRole = if(accountRoleSpinner.isVisible) {
            when {
                accountRoleSpinner.selectedItemPosition == 0 -> "defaultAsset"
                accountRoleSpinner.selectedItemPosition == 1 -> "sharedAsset"
                accountRoleSpinner.selectedItemPosition == 2 -> "savingAsset"
                else -> "ccAsset"
            }
        } else {
            null
        }

        val liabilityType = if(liabilityTypeSpinner.isVisible) {
            when {
                liabilityTypeSpinner.selectedItemPosition == 0 -> "loan"
                liabilityTypeSpinner.selectedItemPosition == 1 -> "debt"
                liabilityTypeSpinner.selectedItemPosition == 2 -> "mortgage"
                else -> "credit card"
            }
        } else {
            null
        }
        val liabilityAmount = if(liabilityAmount_layout.isVisible){
            liabilityAmount_edittext.getString()
        } else {
            null
        }
        val liabilityStartDate = if(liabilityStartDate_layout.isVisible){
            liabilityStartDate_edittext.getString()
        } else {
            null
        }
        val liabilityInterest = if(liabilityInterest_layout.isVisible) {
            liabilityInterest_edittext.getString()
        } else {
            null
        }
        val interest_period= if(interestPeriod.isVisible) {
            when {
                interestPeriod.selectedItemPosition == 0 -> "daily"
                interestPeriod.selectedItemPosition == 1 -> "monthly"
                else -> "yearly"
            }
        } else {
            null
        }
        val creditCardType = if(ccType_layout.isVisible){
            ccType_edittext.getString()
        } else {
            null
        }
        val creditCardDate = if(ccPaymentDate_layout.isVisible){
            ccPaymentDate_edittext.getString()
        } else {
            null
        }
        val accountNumber = if(account_number_edittext.getString().isBlank()){
            null
        } else {
            account_number_edittext.getString()
        }
        val iban = if(iban_edittext.getString().isBlank()){
            null
        } else {
            iban_edittext.getString()
        }
        accountViewModel.addAccounts(accountName_edittext.getString(), accountType,
                currency, networth, accountRole, creditCardType, creditCardDate,
                liabilityType, liabilityAmount, liabilityStartDate, liabilityInterest,
                interest_period, accountNumber, iban).observe(this, Observer {
            val error = it.getError()
            if (it.getResponse() != null) {
                toastSuccess("Account saved!")
                dialog?.dismiss()
            } else if (it.getErrorMessage() != null) {
                toastError(it.getErrorMessage())
            } else if (error != null) {
                if (error.localizedMessage.startsWith("Unable to resolve host")) {
                    val accountData = Data.Builder()
                            .putString("name", accountName_edittext.getString())
                            .putString("type", accountType)
                            .putString("currencyCode", currency)
                            .putInt("includeNetWorth", networth)
                            .putString("accountRole", accountRole)
                            .putString("ccType", creditCardType)
                            .putString("ccMonthlyPaymentDate", creditCardDate)
                            .putString("liabilityType", liabilityType)
                            .putString("liabilityAmount", liabilityAmount)
                            .putString("liabilityStartDate", liabilityStartDate)
                            .putString("interest", liabilityInterest)
                            .putString("interestPeriod", interest_period)
                            .putString("accountNumber", accountNumber)
                            .putString("iban", iban)
                            .build()
                    val accountWork = OneTimeWorkRequest.Builder(AccountWorker::class.java)
                            .setInputData(accountData)
                            .setConstraints(Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build())
                            .build()
                    WorkManager.getInstance().enqueue(accountWork)
                    toastOffline(getString(R.string.data_added_when_user_online, "Account"))
                    dialog?.dismiss()
                } else {
                    toastError(error.localizedMessage)
                }
            } else {
                toastError("Error saving account")
            }
        })
    }
}