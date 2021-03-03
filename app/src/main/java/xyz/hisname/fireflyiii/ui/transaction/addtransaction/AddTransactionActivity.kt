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
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.ActivityAddTransactionBinding
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.util.extension.hideKeyboard

class AddTransactionActivity: BaseActivity() {

    private val transactionType by lazy { intent.getStringExtra("transactionType") }
    private lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        hideKeyboard()
        setBottomNav()
        when (transactionType) {
            "Withdrawal" -> binding.transactionBottomView.selectedItemId = R.id.action_withdraw
            "Deposit" -> binding.transactionBottomView.selectedItemId = R.id.action_deposit
            "Transfer" -> binding.transactionBottomView.selectedItemId = R.id.action_transfer
            else -> binding.transactionBottomView.selectedItemId = R.id.action_withdraw
        }
    }

    private fun setBottomNav(){
        binding.transactionBottomView.setOnNavigationItemSelectedListener{ item ->
            when(item.itemId){
                R.id.action_withdraw -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionPager().apply {
                            arguments = bundleOf("transactionType" to "Withdrawal", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                }
                R.id.action_deposit -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionPager().apply {
                            arguments = bundleOf("transactionType" to "Deposit", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                }
                R.id.action_transfer -> {
                    supportFragmentManager.commit {
                        replace(R.id.addTransactionFrame, AddTransactionPager().apply {
                            arguments = bundleOf("transactionType" to "Transfer", "FROM_TRANSACTION_ACTIVITY" to true)
                        })
                    }
                    true
                } else -> {
                true
            }
            }
        }
    }

}