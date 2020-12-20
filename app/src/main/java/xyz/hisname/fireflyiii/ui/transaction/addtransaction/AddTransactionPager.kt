package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel
import kotlin.random.Random

class AddTransactionPager: BaseFragment() {

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
           handleBack()
        }
        addTransactionText.setOnClickListener {
            val masterId = Random.nextLong()
            addTransactionViewModel.saveData(masterId)
            addTransactionViewModel.memoryCount().observe(viewLifecycleOwner){ count ->
                if(adapter.itemCount == count){
                    addTransactionViewModel.uploadTransaction(group_edittext.getString()).observe(viewLifecycleOwner){ response ->
                        if(response.first){
                            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                            toastSuccess(response.second)
                            handleBack()
                        } else {
                            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                            toastInfo(response.second)
                        }
                    }
                }
            }
        }
    }

    private fun setTabs(){
        addTransactionViewModel.numTabs = tabLayout.tabCount + 1
        adapter = AddTransactionAdapter(this,
                bundleOf("transactionJournalId" to transactionJournalId,
                        "FROM_TRANSACTION_ACTIVITY" to transactionActivity,
                        "transactionType" to transactionType,
                        "SHOULD_HIDE" to isFromFragment,
                        "isFromNotification" to isFromNotification, "isTasker" to isTasker))
        viewPagerLayout.adapter = adapter

        TabLayoutMediator(tabLayout, viewPagerLayout){ tab, position ->
            if(addTransactionViewModel.numTabs == 1){
                tab.text = "No split"
            } else {
                transaction_group_layout.isVisible = true
                tab.text = "Split " + (position + 1)
            }
        }.attach()
        addTransactionViewModel.increaseTab.observe(viewLifecycleOwner){
            addTransactionViewModel.numTabs++
            adapter.setFragmentCount(addTransactionViewModel.numTabs)
        }
        addTransactionViewModel.decreaseTab.observe(viewLifecycleOwner){
            if(tabLayout.selectedTabPosition == 0){
                toastInfo("Unable to remove first split")
            } else {
                addTransactionViewModel.numTabs--
                tabLayout.removeTabAt(tabLayout.selectedTabPosition)
                adapter.setFragmentCount(addTransactionViewModel.numTabs)
                if(addTransactionViewModel.numTabs == 1){
                    transaction_group_layout.isGone = true
                }
            }
        }
    }

    override fun handleBack() {
        if(isFromFragment){
            parentFragmentManager.popBackStack()
        } else {
            if(isTasker){
                requireActivity().onBackPressed()
            } else {
                requireActivity().finish()
            }
        }
    }
}