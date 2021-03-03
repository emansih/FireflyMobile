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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import xyz.hisname.fireflyiii.databinding.FragmentCalculatorBinding
import xyz.hisname.fireflyiii.util.calculator.*
import xyz.hisname.fireflyiii.util.extension.*

class TransactionCalculatorDialog: DialogFragment(), Calculator {

    private val transactionsViewModel by lazy { getViewModel(AddTransactionViewModel::class.java) }
    private var fragmentCalculatorBinding: FragmentCalculatorBinding? = null
    private val binding get() = fragmentCalculatorBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentCalculatorBinding = FragmentCalculatorBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hideKeyboard()
        setCalculator()
        binding.result.text = transactionsViewModel.transactionAmount.value
        binding.formula.text = transactionsViewModel.transactionAmount.value
    }

    private fun setCalculator(){
        val calc = CalculatorImpl(this, requireContext(),
                transactionsViewModel.transactionAmount.value ?: "0")
        binding.btnPlus.setOnClickListener { calc.handleOperation(PLUS) }
        binding.btnMinus.setOnClickListener { calc.handleOperation(MINUS) }
        binding.btnMultiply.setOnClickListener { calc.handleOperation(MULTIPLY) }
        binding.btnDivide.setOnClickListener { calc.handleOperation(DIVIDE) }
        binding.btnPercent.setOnClickListener { calc.handleOperation(PERCENT) }
        binding.btnPower.setOnClickListener { calc.handleOperation(POWER) }
        binding.btnRoot.setOnClickListener { calc.handleOperation(ROOT) }
        binding.btnClear.setOnClickListener { calc.handleClear() }
        binding.btnClear.setOnLongClickListener {
            calc.handleReset()
            true
        }
        binding.btnEquals.setOnClickListener { calc.handleEquals() }
        arrayOf(binding.btnDecimal, binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
                binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9).forEach { button ->
            button.setOnClickListener {
                calc.numpadClicked(it.id)
            }
        }
    }

    override fun setValue(value: String, context: Context) {
        binding.result.text = value
        binding.calculatorFab.setOnClickListener {
            transactionsViewModel.transactionAmount.value = value
            dismiss()
        }
    }

    override fun setFormula(value: String, context: Context) {
        binding.formula.text = value
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = getScreenWidth() - 32
        params?.height = getScreenHeight() - 60
        dialog?.window?.attributes = params
    }
}