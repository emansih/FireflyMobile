package xyz.hisname.fireflyiii.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.currency_bottom_sheet.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.util.EndlessRecyclerViewScrollListener
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.hideKeyboard

class CurrencyListBottomSheet: BottomSheetDialogFragment() {

    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }
    private var dataAdapter = arrayListOf<CurrencyData>()
    private val currencyAdapter by lazy { EnabledCurrencyRecyclerAdapter(dataAdapter) { data: CurrencyData -> itemClicked(data)}}
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private val linearLayout by lazy { LinearLayoutManager(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.currency_bottom_sheet, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        recycler_view.layoutManager = linearLayout
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        recycler_view.adapter = currencyAdapter
        currencyViewModel.getCurrency(1).observe(this) { currencyData ->
            dataAdapter.addAll(currencyData)
            currencyAdapter.update(currencyData)
            currencyAdapter.notifyDataSetChanged()
        }
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayout){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Don't load more when data is refreshing
                currencyViewModel.getCurrency(page + 1).observe(this@CurrencyListBottomSheet) { currencyList ->
                    dataAdapter.clear()
                    dataAdapter.addAll(currencyList)
                    currencyAdapter.update(currencyList)
                    currencyAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun itemClicked(currencyData: CurrencyData){
        currencyViewModel.setCurrencyCode(currencyData.currencyAttributes?.code)
        currencyViewModel.setFullDetails(currencyData.currencyAttributes?.name + " (" + currencyData.currencyAttributes?.code + ")")
        dismiss()
    }
}