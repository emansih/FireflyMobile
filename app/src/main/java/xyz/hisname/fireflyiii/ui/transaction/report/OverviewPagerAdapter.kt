package xyz.hisname.fireflyiii.ui.transaction.report

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import xyz.hisname.fireflyiii.util.SmartFragmentStatePagerAdapter

class OverviewPagerAdapter(fm: FragmentManager): SmartFragmentStatePagerAdapter(fm) {


    override fun getItem(position: Int): Fragment? = when(position){
        0 -> IncomeFragment.newInstance()
        1 -> ExpenseFragment.newInstance()
        else -> null
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> "Income"
        1 -> "Expense"
        else -> ""
    }


}