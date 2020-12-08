package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_calculator.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.calculator.*
import xyz.hisname.fireflyiii.util.extension.*

class TransactionCalculatorDialog: DialogFragment(), Calculator {

    private val transactionsViewModel by lazy { getViewModel(AddTransactionViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_calculator, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        hideKeyboard()
        setCalculator()
        result.text = transactionsViewModel.transactionAmount.value
        formula.text = transactionsViewModel.transactionAmount.value
    }

    private fun setCalculator(){
        val calc = CalculatorImpl(this, requireContext(),
                transactionsViewModel.transactionAmount.value ?: "0")
        btn_plus.setOnClickListener { calc.handleOperation(PLUS) }
        btn_minus.setOnClickListener { calc.handleOperation(MINUS) }
        btn_multiply.setOnClickListener { calc.handleOperation(MULTIPLY) }
        btn_divide.setOnClickListener { calc.handleOperation(DIVIDE) }
        btn_percent.setOnClickListener { calc.handleOperation(PERCENT) }
        btn_power.setOnClickListener { calc.handleOperation(POWER) }
        btn_root.setOnClickListener { calc.handleOperation(ROOT) }

        btn_clear.setOnClickListener { calc.handleClear() }
        btn_clear.setOnLongClickListener {
            calc.handleReset()
            true
        }
        btn_equals.setOnClickListener { calc.handleEquals() }
        arrayOf(btn_decimal, btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9).forEach { button ->
            button.setOnClickListener {
                calc.numpadClicked(it.id)
            }
        }
    }

    override fun setValue(value: String, context: Context) {
        result.text = value
        calculatorFab.setOnClickListener {
            transactionsViewModel.transactionAmount.value = value
            dismiss()
        }
    }

    override fun setFormula(value: String, context: Context) {
        formula.text = value
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = getScreenWidth() - 32
        params?.height = getScreenHeight() - 60
        dialog?.window?.attributes = params
    }
}