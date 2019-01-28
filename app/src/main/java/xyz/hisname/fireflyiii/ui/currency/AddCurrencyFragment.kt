package xyz.hisname.fireflyiii.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_add_currency.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.util.extension.*

class AddCurrencyFragment: BaseAddObjectFragment() {

    private val currencyId by lazy { arguments?.getLong("currencyId") ?: 0L }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_currency, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_currency_layout)
        if(currencyId != 0L){
            currencyViewModel.getCurrencyById(currencyId).observe(this, Observer {
                val currencyAttributes = it[0].currencyAttributes
                name_edittext.setText(currencyAttributes?.name)
                decimal_places_edittext.setText(currencyAttributes?.decimal_places.toString())
                symbol_edittext.setText(currencyAttributes?.symbol)
                code_edittext.setText(currencyAttributes?.code)
                if(currencyAttributes?.enabled == true){
                    enabled_checkbox.isChecked = true
                }
            })
        }
        addCurrencyFab.setOnClickListener {
            hideKeyboard()
            ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
            if(currencyId != 0L){
                updateData()
            } else {
                submitData()
            }
        }
    }

    override fun setIcons(){
        decimal_places_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_dot_circle)
                .color(ContextCompat.getColor(requireContext(), R.color.md_amber_500))
                .sizeDp(24),null, null, null)
        symbol_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_euro_symbol)
                .color(ContextCompat.getColor(requireContext(), R.color.md_pink_800))
                .sizeDp(24),null, null, null)
        code_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_code)
                .color(ContextCompat.getColor(requireContext(), R.color.md_deep_purple_400))
                .sizeDp(24),null, null, null)
        placeHolderToolbar.navigationIcon = navIcon
        addCurrencyFab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
        addCurrencyFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                .sizeDp(24))
    }

    override fun setWidgets(){
        enabled_textview.setOnClickListener {
            enabled_checkbox.performClick()
        }
        placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
    }

    private fun updateData(){
        currencyViewModel.updateCurrency(name_edittext.getString(), code_edittext.getString(),
                symbol_edittext.getString(), decimal_places_edittext.getString(), enabled_checkbox.isChecked)
                .observe(this, Observer { response ->
                    ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
                    val errorMessage = response.getErrorMessage()
                    if (errorMessage != null) {
                        toastError(errorMessage)
                    } else if (response.getResponse() != null) {
                        toastSuccess(resources.getString(R.string.currency_updated, name_edittext.getString()))
                        unReveal(addCurrencyFab)
                    }
                })
    }

    override fun submitData(){
        currencyViewModel.addCurrency(name_edittext.getString(), code_edittext.getString(),
                symbol_edittext.getString(), decimal_places_edittext.getString(), enabled_checkbox.isChecked)
                .observe(this, Observer { response ->
                    ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
                    val errorMessage = response.getErrorMessage()
                    if (errorMessage != null) {
                        toastError(errorMessage)
                    } else if (response.getResponse() != null) {
                        toastSuccess(resources.getString(R.string.currency_created, name_edittext.getString()))
                        unReveal(addCurrencyFab)
                    }
                })
    }

    override fun handleBack() {
        unReveal(dialog_add_currency_layout)
    }
}