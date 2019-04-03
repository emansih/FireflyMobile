package xyz.hisname.fireflyiii.ui.account

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_add_account.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddAccountFragment: BaseAddObjectFragment() {

    private val accountType: String by lazy { arguments?.getString("accountType") ?: "" }
    private val accountId: Long by lazy { arguments?.getLong("accountId") ?: 0L }
    private var currency: String = ""
    private val calendar by lazy { Calendar.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_account, container)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(add_account_layout)
        placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        addAccountFab.setOnClickListener {
            submitData()
        }
    }

    override fun setIcons() {
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        start_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_hourglass_start)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_red_400))
                        .sizeDp(24),null, null, null)
        start_date_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_calendar)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_blue_400))
                        .sizeDp(24),null, null, null)
        interest_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_percent)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_amber_700))
                        .sizeDp(24),null, null, null)
        iban_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_format_list_numbered)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_deep_orange_900))
                        .sizeDp(24),null, null, null)
        bic_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_transfer_within_a_station)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_deep_orange_400))
                        .sizeDp(24),null, null, null)
        account_number_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_confirmation_number)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_brown_600))
                        .sizeDp(24),null, null, null)
        opening_balance_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_open_with)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_red_A100))
                        .sizeDp(24),null, null, null)
        opening_balance_date_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_calendar)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_blue_400))
                        .sizeDp(24),null, null, null)
        virtual_balance_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_balance_scale)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_light_blue_A200))
                        .sizeDp(24),null, null, null)
        addAccountFab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
        addAccountFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_credit_card)
                .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                .sizeDp(24))
    }

    private fun setAccordion(){
        expansionLayout.addListener { _, expanded ->
            if(expanded){
                optionalLayout.isVisible = true
            } else {
                optionalLayout.isInvisible = true
            }
        }
    }

    override fun setWidgets() {
        setAccordion()
        currencyViewModel.currencyCode.observe(this, Observer {
            currency = it
        })
        currencyViewModel.currencyDetails.observe(this, Observer {
            currency_edittext.setText(it)
        })
        currency_edittext.setOnClickListener{
            val currencyListFragment = CurrencyListBottomSheet()
            currencyListFragment.show(requireFragmentManager(), "currencyList" )
        }
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            currency_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
            currency = currencyData?.code ?: ""
        })
        if(accountType == "asset"){
            opening_balance_date_layout.isVisible = true
            opening_balance_date_edittext.setOnClickListener {
                val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    run {
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, monthOfYear)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        opening_balance_date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
                    }
                }
                opening_balance_date_edittext.setOnClickListener {
                    DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                            .show()
                }
            }
            virtual_balance_layout.isVisible = true
            opening_balance_layout.isVisible = true
            accountRoleSpinner.isVisible = true
            currency_layout.isVisible = true
        }
        if(accountType == "liability"){
            currency_layout.isVisible = true
            liabilityTypeSpinner.isVisible = true
            start_amount_layout.isVisible = true
            start_date_layout.isVisible = true
            start_amount_text.isVisible = true
            start_date_edittext.setOnClickListener {
                val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    run {
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, monthOfYear)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        start_date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
                    }
                }
                start_date_edittext.setOnClickListener {
                    DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                            .show()
                }
            }
            interest_layout.isVisible = true
            interestPeriodSpinner.isVisible = true
        }
        includeInNetWorthText.setOnClickListener {
            includeInNetWorthCheck.performClick()
        }
    }

    override fun submitData() {
        val netWorth = includeInNetWorthCheck.isChecked
        val accountRoleSpinner: String? = if(accountRoleSpinner.isVisible){
            when {
                accountRoleSpinner.selectedItemPosition == 0 -> "defaultAsset"
                accountRoleSpinner.selectedItemPosition == 1 -> "sharedAsset"
                accountRoleSpinner.selectedItemPosition == 2 -> "savingAsset"
                accountRoleSpinner.selectedItemPosition == 3 -> "ccAsset"
                else -> "cashWalletAsset "
            }
        } else {
            null
        }
        val iBanString = if (iban_edittext.isBlank()) {
            null
        } else {
            iban_edittext.getString()
        }
        val bicString = if (bic_edittext.isBlank()) {
            null
        } else {
            bic_edittext.getString()
        }
        val accountNumberString = if (account_number_edittext.isBlank()) {
            null
        } else {
            account_number_edittext.getString()
        }
        val openingBalanceValue: String? = if(opening_balance_layout.isVisible){
            opening_balance_edittext.getString()
        } else {
            null
        }
        val openingBalanceDate: String? = if(opening_balance_date_layout.isVisible){
            opening_balance_edittext.getString()
        } else {
            null
        }
        val virtualBalanceString: String? = if(virtual_balance_layout.isVisible){
            virtual_balance_edittext.getString()
        } else {
            null
        }
        val notesText = if (note_edittext.isBlank()) {
            null
        } else {
            note_edittext.getString()
        }
        val currencyToBeSubmitted = if(currency_layout.isVisible && currency.isNotBlank()){
            currency
        } else {
            null
        }
        val liabilityType: String? = if(liabilityTypeSpinner.isVisible){
            when {
                liabilityTypeSpinner.selectedItemPosition == 0 -> "debt"
                liabilityTypeSpinner.selectedItemPosition == 1 -> "loan"
                else -> "mortgage"
            }
        } else {
            null
        }
        val startAmountOfDebt: String? = if(start_amount_layout.isVisible){
            start_amount_edittext.getString()
        } else {
            null
        }
        val startAmountDay: String? = if(start_date_layout.isVisible){
            start_date_edittext.getString()
        } else {
            null
        }
        val interestAmount: String? = if(interest_layout.isVisible){
            interest_edittext.getString()
        } else {
            null
        }
        val interestPeriod: String? = if(interestPeriodSpinner.isVisible){
            when {
                interestPeriodSpinner.selectedItemPosition == 0 -> "daily"
                interestPeriodSpinner.selectedItemPosition == 1 -> "monthly"
                else -> "yearly"
            }
        } else {
            null
        }

        if(accountId != 0L){
            addAccount(description_edittext.getString(), accountType, currencyToBeSubmitted,
                    iBanString, bicString, accountNumberString, openingBalanceValue, openingBalanceDate,
                    accountRoleSpinner, virtualBalanceString, netWorth, notesText, liabilityType, startAmountOfDebt,
                    startAmountDay, interestAmount, interestPeriod)
        } else {
            updateAccount(description_edittext.getString(), accountType, currencyToBeSubmitted,
                    iBanString, bicString, accountNumberString, openingBalanceValue, openingBalanceDate,
                    accountRoleSpinner, virtualBalanceString, netWorth, notesText, liabilityType, startAmountOfDebt,
                    startAmountDay, interestAmount, interestPeriod)
        }
    }

    private fun updateAccount(accountName: String, accountType: String,
                              currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                              openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                              virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                              liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?){
        accountViewModel.updateAccount(accountId,accountName, accountType, currencyCode,
                iban, bic, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod).observe(this, Observer {
            val error = it.getError()
            if (error != null) {
                toastError(error.localizedMessage)
            } else if (it.getResponse() != null) {
                toastSuccess("Account updated")
                handleBack()
            }
        })
    }

    private fun addAccount(accountName: String, accountType: String,
                           currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                           openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                           virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                           liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?){
        accountViewModel.addAccounts(accountName, accountType, currencyCode,
                iban, bic, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod).observe(this, Observer {
            val error = it.getError()
            if (error != null) {
                if(error.localizedMessage.startsWith("Unable to resolve host")) {
                    toastOffline(getString(R.string.data_added_when_user_online, "Account"))
                    handleBack()
                } else {
                    toastError(error.localizedMessage)
                }
            } else if (it.getResponse() != null) {
                toastSuccess("Account saved")
                handleBack()
            }
        })
    }

    override fun handleBack() {
        unReveal(add_account_layout)
    }

}