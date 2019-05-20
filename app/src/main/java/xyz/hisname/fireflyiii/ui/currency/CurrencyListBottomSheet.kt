package xyz.hisname.fireflyiii.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.currency_bottom_sheet.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.hideKeyboard
import java.util.*


class CurrencyListBottomSheet: BottomSheetDialogFragment() {

    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private var dataAdapter = arrayListOf<CurrencyData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.currency_bottom_sheet, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
        currencyViewModel.getEnabledCurrency().observe(this, Observer { currencyData ->
            dataAdapter = ArrayList(currencyData)
            recycler_view.adapter = EnabledCurrencyRecyclerAdapter(currencyData) { data: CurrencyData ->
                itemClicked(data)
            }.apply {
                update(dataAdapter)
            }
        })
    }

    private fun itemClicked(currencyData: CurrencyData){
        currencyViewModel.setCurrencyCode(currencyData.currencyAttributes?.code)
        currencyViewModel.setFullDetails(currencyData.currencyAttributes?.name + " (" + currencyData.currencyAttributes?.code + ")")
        dismiss()
    }
}