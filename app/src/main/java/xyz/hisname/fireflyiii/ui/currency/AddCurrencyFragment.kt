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
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentAddCurrencyBinding
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.util.extension.*

class AddCurrencyFragment: BaseAddObjectFragment() {

    private val currencyId by lazy { arguments?.getLong("currencyId") ?: 0L }
    private val currencyViewModel by lazy { getImprovedViewModel(AddCurrencyViewModel::class.java) }
    private var fragmentAddCurrencyBinding: FragmentAddCurrencyBinding? = null
    private val binding get() = fragmentAddCurrencyBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAddCurrencyBinding = FragmentAddCurrencyBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(binding.dialogAddCurrencyLayout)
        if(currencyId != 0L){
            currencyViewModel.getCurrencyById(currencyId).observe(viewLifecycleOwner) {
                val currencyAttributes = it[0].currencyAttributes
                binding.nameEdittext.setText(currencyAttributes.name)
                binding.decimalPlacesEdittext.setText(currencyAttributes.decimal_places.toString())
                binding.symbolEdittext.setText(currencyAttributes.symbol)
                binding.codeEdittext.setText(currencyAttributes.code)
                if(currencyAttributes.enabled){
                    binding.enabledCheckbox.isChecked = true
                }
                if(currencyAttributes.currencyDefault){
                    binding.defaultCheckbox.isChecked = true
                }
            }
        }
        setFab()
    }

    private fun setFab(){
        if(currencyId != 0L){
            binding.addCurrencyFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        binding.addCurrencyFab.setOnClickListener {
            hideKeyboard()
            if(currencyId != 0L){
                updateData()
            } else {
                submitData()
            }
        }
    }

    override fun setIcons(){
        binding.decimalPlacesEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_dot_circle
            colorRes = R.color.md_amber_500
            sizeDp = 24
        },null, null, null)
        binding.symbolEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_euro_symbol
            colorRes = R.color.md_pink_800
            sizeDp = 24
        },null, null, null)
        binding.codeEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_code
            colorRes = R.color.md_deep_purple_400
            sizeDp = 24
        },null, null, null)
        binding.addCurrencyFab.setBackgroundColor(getCompatColor( R.color.colorPrimaryDark))
        binding.addCurrencyFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
    }

    override fun setWidgets(){
        binding.enabledTextview.setOnClickListener {
            binding.enabledCheckbox.performClick()
        }
        binding.defaultTextview.setOnClickListener {
            binding.defaultCheckbox.performClick()
        }
        binding.placeHolderToolbar.setNavigationOnClickListener {
            unReveal(binding.dialogAddCurrencyLayout)
            extendedFab.isVisible = true
        }
        currencyViewModel.isLoading.observe(viewLifecycleOwner) { loader ->
            if(loader){
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun updateData(){
        currencyViewModel.updateCurrency(binding.nameEdittext.getString(), binding.codeEdittext.getString(),
                binding.symbolEdittext.getString(), binding.decimalPlacesEdittext.getString(),
                binding.enabledCheckbox.isChecked, binding.defaultCheckbox.isChecked).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(resources.getString(R.string.currency_updated, binding.nameEdittext.getString()))
                unReveal(binding.addCurrencyFab)
                extendedFab.isVisible = true
            } else {
                toastInfo(response.second)
            }
        }
    }

    override fun submitData(){
        currencyViewModel.addCurrency(binding.nameEdittext.getString(), binding.codeEdittext.getString(),
                binding.symbolEdittext.getString(), binding.decimalPlacesEdittext.getString(),
                binding.enabledCheckbox.isChecked, binding.defaultCheckbox.isChecked).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(resources.getString(R.string.currency_created, binding.nameEdittext.getString()))
                unReveal(binding.addCurrencyFab)
            } else {
                toastInfo(response.second)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAddCurrencyBinding = null
    }
}