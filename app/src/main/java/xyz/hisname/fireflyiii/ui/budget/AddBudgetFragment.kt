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

package xyz.hisname.fireflyiii.ui.budget

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_add_budget.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.budget.budgetList.BudgetType
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyBottomSheetViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel

class AddBudgetFragment: BaseAddObjectFragment() {

    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val addBudgetViewModel by lazy { getImprovedViewModel(AddBudgetViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_budget, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Remove this when the time is right
        addBudgetViewModel.unSupportedVersion.observe(viewLifecycleOwner){ unSupported ->
            if(unSupported){
                val message = Html.fromHtml("You are using an unsupported version of Firefly III which contains a bug. Proceed at your own risk. Follow <a href=\"https://github.com/firefly-iii/firefly-iii/issues/4394\">this issue</a> for more info")
                val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Uh oh...")
                        .setMessage(message)
                        .setPositiveButton("OK"){ _ , _ -> }
                        .show()
                dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    override fun setIcons() {
        currencyEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        amountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_dollar_sign
                    colorRes = R.color.md_yellow_A700
                    sizeDp = 24
                },null, null, null)
        addBudgetFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
    }

    override fun setWidgets() {
        disableField()
        autoBudget.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0){
                    autoBudgetPeriod.isVisible = true
                    amountLayout.isVisible = true
                    currencyLayout.isVisible = true
                } else {
                    disableField()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        setCurrency()
        placeHolderToolbar.setNavigationOnClickListener { handleBack() }
        addBudgetFab.setOnClickListener {
            submitData()
        }
    }

    override fun submitData() {
        ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
        var amount: String? = null
        var currency: String? = null
        var freq: String? = null
        if(autoBudget.selectedItemPosition != 0){
            amount =  amountEdittext.getString()
            currency = addBudgetViewModel.currency
            freq = when(autoBudgetPeriod.selectedItemPosition){
                0 -> "weekly"
                1 -> "monthly"
                2 -> "quarterly"
                3 -> "half-year"
                4 -> "yearly"
                else -> ""
            }
        }
        val budgetType = when (autoBudget.selectedItemPosition) {
            1 -> {
                BudgetType.RESET
            }
            2 -> {
                BudgetType.ROLLOVER
            }
            else -> {
                BudgetType.NONE
            }
        }
        addBudgetViewModel.addBudget(budgetNameEditText.getString(), budgetType, currency,
                amount, freq).observe(viewLifecycleOwner) { response ->
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

    private fun setCurrency(){
        currencyEdittext.setOnClickListener {
            if(autoBudget.selectedItemPosition != 0){
                CurrencyListBottomSheet().show(childFragmentManager, "currencyList" )
            }
        }

        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currencyCode ->
            addBudgetViewModel.currency = currencyCode
        }

        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) { currency ->
            currencyEdittext.setText(currency)

        }
        addBudgetViewModel.getDefaultCurrency().observe(viewLifecycleOwner){ currency ->
            currencyEdittext.setText(currency)
        }
    }

    private fun disableField(){
        autoBudgetPeriod.isVisible = false
        amountLayout.isVisible = false
        currencyLayout.isVisible = false
    }

    private fun handleBack() {
        unReveal(addBudgetLayout)
    }
}