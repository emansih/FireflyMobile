package xyz.hisname.fireflyiii.ui.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.display
import xyz.hisname.fireflyiii.util.extension.hideFab
import xyz.hisname.fireflyiii.util.extension.toastError
import java.util.*

class ListAccountFragment: BaseFragment() {

    private var dataAdapter = ArrayList<AccountData>()
    private val accountType by lazy { arguments?.getString("accountType") ?: "" }
    private val noAccountImage by lazy { requireActivity().findViewById<ImageView>(R.id.listImage) }
    private val noAccountText by lazy { requireActivity().findViewById<TextView>(R.id.listText) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        swipeContainer.isRefreshing = accountViewModel.isLoading.value == true
        runLayoutAnimation(recycler_view)
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        when (accountType) {
            "asset" -> accountViewModel.getAssetAccounts().observe(this, Observer {
                if(it.isEmpty()){
                    recycler_view.isGone = true
                    noAccountImage.isVisible = true
                    noAccountText.isVisible = true
                    noAccountImage.setImageDrawable(IconicsDrawable(requireContext())
                            .icon(FontAwesome.Icon.faw_money_bill))
                    noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.asset_account))
                } else {
                    noAccountText.isGone = true
                    noAccountImage.isGone = true
                    recycler_view.isVisible = true
                    recycler_view.adapter = AccountRecyclerAdapter(it) { data: AccountData -> itemClicked(data) }
                }            })
            "expense" -> accountViewModel.getExpenseAccounts().observe(this, Observer {
                if(it.isEmpty()){
                    recycler_view.isGone = true
                    noAccountImage.isVisible = true
                    noAccountText.isVisible = true
                    noAccountImage.setImageDrawable(IconicsDrawable(requireContext())
                            .icon(FontAwesome.Icon.faw_shopping_cart))
                    noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.expense_account))
                } else {
                    noAccountText.isGone = true
                    noAccountImage.isGone = true
                    recycler_view.isVisible = true
                    recycler_view.adapter = AccountRecyclerAdapter(it) { data: AccountData -> itemClicked(data) }
                }            })
            "revenue" -> accountViewModel.getRevenueAccounts().observe(this, Observer {
                if(it.isEmpty()){
                    recycler_view.isGone = true
                    noAccountImage.isVisible = true
                    noAccountText.isVisible = true
                    noAccountImage.setImageDrawable(IconicsDrawable(requireContext())
                            .icon(FontAwesome.Icon.faw_download))
                    noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.revenue_account))
                } else {
                    noAccountText.isGone = true
                    noAccountImage.isGone = true
                    recycler_view.isVisible = true
                    recycler_view.adapter = AccountRecyclerAdapter(it) { data: AccountData -> itemClicked(data) }
                }            })
            "liability" -> accountViewModel.getLiabilityAccounts().observe(this, Observer {
                if(it.isEmpty()){
                    recycler_view.isGone = true
                    noAccountImage.isVisible = true
                    noAccountText.isVisible = true
                    noAccountImage.setImageDrawable(IconicsDrawable(requireContext())
                            .icon(FontAwesome.Icon.faw_ticket_alt))
                    noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.revenue_account))
                } else {
                    noAccountText.isGone = true
                    noAccountImage.isGone = true
                    recycler_view.isVisible = true
                    recycler_view.adapter = AccountRecyclerAdapter(it) { data: AccountData -> itemClicked(data) }
                }
            })
        }
        accountViewModel.apiResponse.observe(this, Observer {
            toastError(it)
        })
    }

    private fun itemClicked(data: AccountData){
        val bundle = bundleOf("accountId" to data.accountId)
        requireFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, AccountDetailFragment().apply { arguments = bundle })
                .commit()
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            displayView()
        }
    }

    private fun initFab(){
        fab.display {
            fab.isClickable = false
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, AddAccountFragment().apply{
                    arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2, "accountType" to accountType)
                })
                addToBackStack(null)
            }
            fab.isClickable = true
        }
        recycler_view.hideFab(fab)
    }

    private fun convertString(): String{
        return when {
            Objects.equals(accountType, "asset") -> resources.getString(R.string.asset_account)
            Objects.equals(accountType, "expense") -> resources.getString(R.string.expense_account)
            Objects.equals(accountType, "revenue") -> resources.getString(R.string.revenue_account)
            Objects.equals(accountType, "liability") -> resources.getString(R.string.liability_account)
            else -> "Accounts"
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = convertString()
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = convertString()
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }

    override fun handleBack() {
        requireFragmentManager().popBackStack()
    }

}