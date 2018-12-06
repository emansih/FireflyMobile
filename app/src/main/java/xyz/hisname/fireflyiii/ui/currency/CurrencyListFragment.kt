package xyz.hisname.fireflyiii.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_currency.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.currency.CurrencyViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.hideKeyboard
import java.util.*


class CurrencyListFragment: BottomSheetDialogFragment() {

    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_currency, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard()
        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
            adapter?.notifyDataSetChanged()
        }
        currencyViewModel.getCurrency().observe(this, Observer {currencyData ->
            currencyData?.sortWith(Comparator { initial, after ->
                initial.currencyAttributes?.name!!.compareTo(after.currencyAttributes?.name!!)
            })
            recycler_view.adapter = CurrencyRecyclerAdapter(currencyData) { data: CurrencyData -> itemClicked(data) }
        })

        currencyViewModel.isLoading.observe(this, Observer {
            if(it == true){
                ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
            }
        })
    }

    private fun itemClicked(currencyData: CurrencyData){
        currencyViewModel.setCurrencyCode(currencyData.currencyAttributes?.code)
        currencyViewModel.setFullDetails(currencyData.currencyAttributes?.name + " (" + currencyData.currencyAttributes?.code + ")")
        dismiss()
    }
}