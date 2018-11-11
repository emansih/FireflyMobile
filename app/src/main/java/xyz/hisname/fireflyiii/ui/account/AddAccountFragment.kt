package xyz.hisname.fireflyiii.ui.account

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_add_account.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.AccountsViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.CurrencyViewModel
import xyz.hisname.fireflyiii.repository.workers.account.AccountWorker
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddAccountFragment: BaseFragment(){

    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_account, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setUpWidget()
        currencyViewModel.currencyCode.observe(this, Observer {
            currencyCode.setText(it)
        })
    }

    private fun setUpWidget(){
        val accountTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayListOf(
                "Asset" , "Expense", "Revenue", "Liability"
                ))
        accountTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountType.adapter = accountTypeAdapter
        accountType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val index = parent.selectedItemPosition
                accountRole.isVisible = index == 0
                liabilityType.isVisible = index == 3
                liabilityAmount.isVisible = index == 3
                liabilityStartDate.isVisible = index == 3
                liabilityInterest.isVisible = index == 3
                interestPeriod.isVisible = index == 3
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        val accountRoleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayListOf(
                "Default Asset", "Shared Asset", "Saving Asset", "Credit Card Asset"
        ))
        accountRoleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountRole.adapter = accountRoleAdapter
        accountRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val index = parent.selectedItemPosition
                ccType.isVisible = index == 3
                ccPaymentDate.isVisible = index == 3
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        currencyCode.setOnClickListener{
            val currencyListFragment = CurrencyListFragment()
            currencyListFragment.show(requireFragmentManager(), "currencyList" )
        }
        ccPaymentDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val date = DatePickerDialog.OnDateSetListener {
                _, year, monthOfYear, dayOfMonth ->
                run {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    ccPaymentDate.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
                }
            }
            ccPaymentDate.setOnClickListener {
                DatePickerDialog(requireContext(), date, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        .show()
            }
        }
        val liabilityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayListOf(
                "Loan" , "Debt", "Mortgage", "Credit Card"
        ))
        liabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        liabilityType.adapter = liabilityAdapter
        val interestAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayListOf(
                "Daily" , "Monthly", "Yearly"
        ))
        interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        interestPeriod.adapter = interestAdapter
        netWorthText.setOnClickListener {
            netWorthCheckbox.performClick()
        }
    }

    private fun submitData(){
        hideKeyboard()
        ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
        val networth = if(netWorthCheckbox.isChecked){
            1
        } else {
            0
        }
        val account_type = when {
            accountType.selectedItemPosition == 0 -> "asset"
            accountType.selectedItemPosition == 1 -> "expense"
            accountType.selectedItemPosition == 2 -> "revenue "
            else -> "liability"
        }
        val role = if(accountRole.isVisible) {
            when {
                accountRole.selectedItemPosition == 0 -> "defaultAsset"
                accountRole.selectedItemPosition == 1 -> "sharedAsset"
                accountRole.selectedItemPosition == 2 -> "savingAsset"
                else -> "ccAsset"
            }
        } else {
            null
        }

        val liability_type = if(liabilityType.isVisible) {
            when {
                liabilityType.selectedItemPosition == 0 -> "loan"
                liabilityType.selectedItemPosition == 1 -> "debt"
                liabilityType.selectedItemPosition == 2 -> "mortgage"
                else -> "credit card"
            }
        } else {
            null
        }
        val liability_amount = if(liabilityAmount.isVisible){
            liabilityAmount.getString()
        } else {
            null
        }
        val liability_start_date = if(liabilityStartDate.isVisible){
            liabilityStartDate.getString()
        } else {
            null
        }
        val liability_interest = if(liabilityInterest.isVisible) {
            liabilityInterest.getString()
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
        val creditCardType = if(ccType.isVisible){
            ccType.getString()
        } else {
            null
        }
        val creditCardDate = if(ccPaymentDate.isVisible){
            ccPaymentDate.getString()
        } else {
            null
        }

        accountViewModel.addAccounts(baseUrl, accessToken, accountName.getString(), account_type,
                currencyCode.getString(), networth, role, creditCardType , creditCardDate,
                liability_type, liability_amount, liability_start_date, liability_interest,
                interest_period, accountNumber.getString())
                .observe(this, Observer {
                    val error = it.getError()
                    ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                    if(it.getResponse() != null){
                        toastSuccess("Account saved!")
                        requireFragmentManager().popBackStack()
                    } else if(it.getErrorMessage() != null){
                        toastError(it.getErrorMessage())
                    } else if(error != null){
                        if(error.localizedMessage.startsWith("Unable to resolve host")){
                            val accountData = Data.Builder()
                                    .putString("name", accountName.getString())
                                    .putString("type", account_type)
                                    .putString("currencyCode", currencyCode.getString())
                                    .putInt("includeNetWorth", networth)
                                    .putString("accountRole", role)
                                    .putString("ccType", creditCardType)
                                    .putString("ccMonthlyPaymentDate", creditCardDate)
                                    .putString("liabilityType", liability_type)
                                    .putString("liabilityAmount", liability_amount)
                                    .putString("liabilityStartDate", liability_start_date)
                                    .putString("interest", liability_interest)
                                    .putString("interestPeriod", interest_period)
                                    .putString("accountNumber", accountNumber.getString())
                                    .build()
                            val accountWork = OneTimeWorkRequest.Builder(AccountWorker::class.java)
                                    .setInputData(accountData)
                                    .setConstraints(Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            .build())
                                    .build()
                            WorkManager.getInstance().enqueue(accountWork)
                            toastOffline(getString(R.string.data_added_when_user_online, "Account"))
                            requireFragmentManager().popBackStack()
                        } else {
                            toastError("Error saving account")
                        }
                    } else {
                        toastError("Error saving account")
                    }
                })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.menu_item_save) {
            submitData()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Add Account"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Add Account"
    }
}