package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getString
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo

class AddTransactionPager: Fragment() {

    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId") ?: 0 }
    private val transactionActivity by lazy { arguments?.getBoolean("FROM_TRANSACTION_ACTIVITY") }
    private val isTasker by lazy { arguments?.getBoolean("isTasker") ?: false }
    private val isFromNotification by lazy { requireActivity().intent.extras?.getBoolean("isFromNotification") ?: false }
    private val isFromFragment by lazy { arguments?.getBoolean("SHOULD_HIDE") ?: false }
    private val transactionType by lazy { arguments?.getString("transactionType") ?: "" }
    private lateinit var adapter: AddTransactionAdapter
    private val addTransactionViewModel by lazy { getViewModel(AddTransactionViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.add_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTabs()
        setToolbar()
    }

    private fun setToolbar(){
        materialToolbar.setNavigationOnClickListener {
           // TODO
        }
        addTransactionText.setOnClickListener {
            addTransactionViewModel.saveData.postValue(true)
            addTransactionViewModel.memoryCount().observe(viewLifecycleOwner){ count ->
                if(adapter.itemCount == count){
                    addTransactionViewModel.uploadTransaction(group_edittext.getString())
                }
            }
        }
    }

    private fun setTabs(){
        var numTabs = tabLayout.tabCount + 1
        adapter = AddTransactionAdapter(this,
                bundleOf("transactionJournalId" to transactionJournalId,
                        "FROM_TRANSACTION_ACTIVITY" to transactionActivity,
                        "transactionType" to transactionType,
                        "SHOULD_HIDE" to isFromFragment,
                        "isFromNotification" to isFromNotification, "isTasker" to isTasker))
        viewPagerLayout.adapter = adapter
        splitTransactionButton.setOnClickListener {
            numTabs++
            adapter.setFragmentCount(numTabs)
        }
        deleteTransactionButton.setOnClickListener {
            if(tabLayout.selectedTabPosition == 0){
                toastInfo("Unable to remove first split")
            } else {
                numTabs--
                tabLayout.removeTabAt(tabLayout.selectedTabPosition)
            }
        }
        TabLayoutMediator(tabLayout, viewPagerLayout){ tab, position ->
            if(numTabs == 1){
                tab.text = "No split"
            } else {
                transaction_group_layout.isVisible = true
                deleteTransactionButton.isVisible = true
                tab.text = "Split " + (position + 1)
            }
        }.attach()
    }
}