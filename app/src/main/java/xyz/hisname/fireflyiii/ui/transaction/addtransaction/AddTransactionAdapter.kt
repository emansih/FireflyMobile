package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AddTransactionAdapter(fragment: Fragment,
                            private val transactionBundle: Bundle): FragmentStateAdapter(fragment){

    private var fragmentCount = 1

    fun setFragmentCount(fragmentCount: Int){
        this.fragmentCount = fragmentCount
        notifyDataSetChanged()
    }

    override fun getItemCount() = fragmentCount

    override fun createFragment(position: Int): Fragment {
        val addTransactionFragment = AddTransactionFragment()
        addTransactionFragment.arguments = transactionBundle
        return addTransactionFragment
    }
}