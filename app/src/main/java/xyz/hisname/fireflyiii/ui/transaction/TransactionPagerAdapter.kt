package xyz.hisname.fireflyiii.ui.transaction

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.SmartFragmentStatePagerAdapter

class TransactionPagerAdapter(fragmentManager: FragmentManager, private val context: Context): SmartFragmentStatePagerAdapter<Fragment>(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        val addTransactionPager = AddTransactionPager()
        return when(position){
            0 -> addTransactionPager.apply {
                arguments = bundleOf("transactionType" to "Deposit")
            }
            1 -> addTransactionPager.apply {
                arguments = bundleOf("transactionType" to "Withdrawal")
            }
            2 -> addTransactionPager.apply {
                arguments = bundleOf("transactionType" to "Transfer")
            }
            else -> Fragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when(position){
            0 -> context.resources.getString(R.string.deposit)
            1 -> context.resources.getString(R.string.withdrawal)
            2 -> context.resources.getString(R.string.transfer)
            else -> ""
        }
    }
}