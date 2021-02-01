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

package xyz.hisname.fireflyiii.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_add_currency.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.util.extension.*

class AddCurrencyFragment: BaseAddObjectFragment() {

    private val currencyId by lazy { arguments?.getLong("currencyId") ?: 0L }
    private val currencyViewModel by lazy { getImprovedViewModel(AddCurrencyViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_currency, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_currency_layout)
        if(currencyId != 0L){
            currencyViewModel.getCurrencyById(currencyId).observe(viewLifecycleOwner) {
                val currencyAttributes = it[0].currencyAttributes
                name_edittext.setText(currencyAttributes.name)
                decimal_places_edittext.setText(currencyAttributes.decimal_places.toString())
                symbol_edittext.setText(currencyAttributes.symbol)
                code_edittext.setText(currencyAttributes.code)
                if(currencyAttributes.enabled){
                    enabled_checkbox.isChecked = true
                }
                if(currencyAttributes.currencyDefault){
                    default_checkbox.isChecked = true
                }
            }
        }
        setFab()
    }

    private fun setFab(){
        if(currencyId != 0L){
            addCurrencyFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        addCurrencyFab.setOnClickListener {
            hideKeyboard()
            if(currencyId != 0L){
                updateData()
            } else {
                submitData()
            }
        }
    }

    override fun setIcons(){
        decimal_places_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_dot_circle
            colorRes = R.color.md_amber_500
            sizeDp = 24
        },null, null, null)
        symbol_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_euro_symbol
            colorRes = R.color.md_pink_800
            sizeDp = 24
        },null, null, null)
        code_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_code
            colorRes = R.color.md_deep_purple_400
            sizeDp = 24
        },null, null, null)
        addCurrencyFab.setBackgroundColor(getCompatColor( R.color.colorPrimaryDark))
        addCurrencyFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
    }

    override fun setWidgets(){
        enabled_textview.setOnClickListener {
            enabled_checkbox.performClick()
        }
        default_textview.setOnClickListener {
            default_checkbox.performClick()
        }
        placeHolderToolbar.setNavigationOnClickListener {
            unReveal(dialog_add_currency_layout)
            extendedFab.isVisible = true
        }
        currencyViewModel.isLoading.observe(viewLifecycleOwner) { loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
    }

    private fun updateData(){
        currencyViewModel.updateCurrency(name_edittext.getString(), code_edittext.getString(),
                symbol_edittext.getString(), decimal_places_edittext.getString(),
                enabled_checkbox.isChecked, default_checkbox.isChecked).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(resources.getString(R.string.currency_updated, name_edittext.getString()))
                unReveal(addCurrencyFab)
                extendedFab.isVisible = true
            } else {
                toastInfo(response.second)
            }
        }
    }

    override fun submitData(){
        currencyViewModel.addCurrency(name_edittext.getString(), code_edittext.getString(),
                symbol_edittext.getString(), decimal_places_edittext.getString(),
                enabled_checkbox.isChecked, default_checkbox.isChecked).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(resources.getString(R.string.currency_created, name_edittext.getString()))
                unReveal(addCurrencyFab)
            } else {
                toastInfo(response.second)
            }
        }
    }
}