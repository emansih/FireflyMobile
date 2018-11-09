package xyz.hisname.fireflyiii.ui.currency

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
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
import xyz.hisname.fireflyiii.repository.viewmodel.CurrencyViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import java.util.*


class CurrencyListFragment: BottomSheetDialogFragment() {

    private val currencyViewModel by lazy { getViewModel(CurrencyViewModel::class.java)}
    // Note: For some really weird reason, we are not able to pass bundles to this fragment
    private val sharedPref: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val baseUrl: String by lazy { sharedPref.getString("fireflyUrl","")}
    private val accessToken: String by lazy { sharedPref.getString("access_token","") }
    private lateinit var listener: OnCompleteListener


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_currency, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
        val viewModel = currencyViewModel.getCurrency(baseUrl,accessToken)
        recycler_view.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
            adapter?.notifyDataSetChanged()
        }
        viewModel.databaseData?.observe(this, Observer {
            ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
            it.sortWith(Comparator { initial, after ->
                initial.currencyAttributes?.name!!.compareTo(after.currencyAttributes?.name!!)
            })
            recycler_view.adapter = CurrencyRecyclerAdapter(it) { data: CurrencyData -> itemClicked(data) }
        })

        viewModel.apiResponse.observe(this, Observer {
            ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
            if(it.getError() != null){
                toastError(it.getError()?.message)
            }
        })
    }

    private fun itemClicked(currencyData: CurrencyData){
        this.listener.onCurrencyClickListener(currencyData.currencyAttributes?.code!!)
        dismiss()
    }

    fun setCurrencyListener(listener: OnCompleteListener){
        this.listener = listener
    }

    interface OnCompleteListener {
        fun onCurrencyClickListener(currency: String)
    }

}