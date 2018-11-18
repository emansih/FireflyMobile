package xyz.hisname.fireflyiii.ui.account

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_add_account.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.CurrencyViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.hideKeyboard
import java.util.*

abstract class BaseAccountFragment: BaseFragment() {

    val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    val accountArgument: String by lazy { arguments?.getString("accountType") ?: "" }
    lateinit var currency: String
    private val calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_account, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setWidget()
    }

    private fun setWidget(){
        currencyViewModel.currencyCode.observe(this, Observer {
            currency = it
        })
        currencyViewModel.currencyDetails.observe(this, Observer {
            currencyCode.setText(it)
        })
        currencyCode.setOnClickListener{
            val currencyListFragment = CurrencyListFragment()
            currencyListFragment.show(requireFragmentManager(), "currencyList" )
        }
        ccPaymentDate.setOnClickListener {
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
        liabilityStartDate.setOnClickListener {
            val date = DatePickerDialog.OnDateSetListener {
                _, year, monthOfYear, dayOfMonth ->
                run {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    liabilityStartDate.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
                }
            }
            liabilityStartDate.setOnClickListener {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.menu_item_save) {
            ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            hideKeyboard()
            submitData()
        }
        return true
    }

    protected fun convertString(): String{
        return when {
            Objects.equals(accountArgument, "Asset Account") -> "asset"
            Objects.equals(accountArgument, "Expense Account") -> "expense"
            Objects.equals(accountArgument, "Revenue Account") -> "revenue"
            Objects.equals(accountArgument, "Liability Account") -> "liability"
            else -> "Account"
        }
    }


    abstract fun submitData()
}