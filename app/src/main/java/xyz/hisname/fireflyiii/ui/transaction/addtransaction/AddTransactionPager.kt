/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import xyz.hisname.fireflyiii.databinding.AddTransactionBinding
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.random.Random

class AddTransactionPager: BaseFragment() {

    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId") ?: 0 }
    private val transactionActivity by lazy { arguments?.getBoolean("FROM_TRANSACTION_ACTIVITY") }
    private val isTasker by lazy { arguments?.getBoolean("isTasker") ?: false }
    private val isFromNotification by lazy { requireActivity().intent.extras?.getBoolean("isFromNotification") ?: false }
    private val isFromFragment by lazy { arguments?.getBoolean("SHOULD_HIDE") ?: false }
    private val transactionType by lazy { arguments?.getString("transactionType") ?: "" }
    private val adapter by lazy { AddTransactionAdapter(this,
            bundleOf("transactionJournalId" to transactionJournalId,
                    "FROM_TRANSACTION_ACTIVITY" to transactionActivity,
                    "transactionType" to transactionType,
                    "SHOULD_HIDE" to isFromFragment,
                    "isFromNotification" to isFromNotification, "isTasker" to isTasker)) }
    private val addTransactionViewModel by lazy {
        ViewModelProvider(this).get(AddTransactionViewModel::class.java)
    }

    private var addTransactionBinding: AddTransactionBinding? = null
    private val binding get() = addTransactionBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        addTransactionBinding = AddTransactionBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTabs()
        setToolbar()
        if(isTasker || transactionJournalId != 0L){
            binding.tabLayout.isGone = true
            binding.transactionGroupLayout.isGone = true
        }
    }

    private fun setToolbar(){
        binding.materialToolbar.setNavigationOnClickListener {
           handleBack()
        }
        binding.addTransactionText.setOnClickListener {
            ProgressBar.animateView(binding.progressLayout.progressOverlay, View.VISIBLE, 0.4f, 200)
            val masterId = Random.nextLong()
            addTransactionViewModel.saveData(masterId)
            addTransactionViewModel.memoryCount().observe(viewLifecycleOwner){ count ->
                if(adapter.itemCount == count){
                    addTransactionViewModel.uploadTransaction(binding.groupEdittext.getString()).observe(viewLifecycleOwner){ response ->
                        ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f, 200)
                        if(response.first){
                            toastSuccess(response.second)
                            handleBack()
                        } else {
                            toastInfo(response.second)
                        }
                    }
                }
            }
        }
        addTransactionViewModel.isLoading.observe(viewLifecycleOwner){ loading ->
            if(!loading){
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun setTabs(){
        addTransactionViewModel.numTabs = binding.tabLayout.tabCount + 1
        binding.viewPagerLayout.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPagerLayout){ tab, position ->
            if(addTransactionViewModel.numTabs == 1){
                tab.text = "No split"
            } else {
                binding.transactionGroupLayout.isVisible = true
                tab.text = "Split " + (position + 1)
            }
        }.attach()
        addTransactionViewModel.increaseTab.observe(viewLifecycleOwner){
            addTransactionViewModel.numTabs++
            adapter.setFragmentCount(addTransactionViewModel.numTabs)
        }
        addTransactionViewModel.decreaseTab.observe(viewLifecycleOwner){
            if(binding.tabLayout.selectedTabPosition == 0){
                toastInfo("Unable to remove first split")
            } else {
                addTransactionViewModel.numTabs--
                binding.tabLayout.removeTabAt(binding.tabLayout.selectedTabPosition)
                adapter.setFragmentCount(addTransactionViewModel.numTabs)
                if(addTransactionViewModel.numTabs == 1){
                    binding.transactionGroupLayout.isGone = true
                }
            }
        }
    }

    private fun handleBack() {
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