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
import androidx.lifecycle.observe
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.display
import xyz.hisname.fireflyiii.util.extension.enableDragDrop
import xyz.hisname.fireflyiii.util.extension.hideFab
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
        recycler_view.enableDragDrop(extendedFab)
    }

    private fun displayView(){
        swipeContainer.isRefreshing = true
        runLayoutAnimation(recycler_view)
        accountViewModel.getAccountByType(accountType).observe(viewLifecycleOwner){ accountData ->
            if(accountData.isEmpty()){
                recycler_view.isGone = true
                noAccountImage.isVisible = true
                noAccountText.isVisible = true
                when (accountType) {
                    "asset" -> {
                        noAccountImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_money_bill))
                        noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.asset_account))
                    }
                    "expense" -> {
                        noAccountImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_shopping_cart))
                        noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.expense_account))
                    }
                    "revenue" -> {
                        noAccountImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_download))
                        noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.revenue_account))
                    }
                    "liabilities" -> {
                        noAccountImage.setImageDrawable(IconicsDrawable(requireContext(), FontAwesome.Icon.faw_ticket_alt))
                        noAccountText.text = resources.getString(R.string.no_account_found, resources.getString(R.string.revenue_account))
                    }
                }
            } else {
                noAccountText.isGone = true
                noAccountImage.isGone = true
                recycler_view.isVisible = true
                recycler_view.adapter = AccountRecyclerAdapter(accountData) { data: AccountData -> itemClicked(data) }
            }
            swipeContainer.isRefreshing = false
        }
    }


    private fun itemClicked(data: AccountData){
        val bundle = bundleOf("accountId" to data.accountId, "accountType" to accountType)
        parentFragmentManager.commit {
            addToBackStack(null)
            replace(R.id.fragment_container, AccountDetailFragment().apply { arguments = bundle })
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            displayView()
        }
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddAccountFragment().apply{
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2, "accountType" to accountType)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
    }

    private fun convertString(): String{
        return when {
            Objects.equals(accountType, "asset") -> resources.getString(R.string.asset_account)
            Objects.equals(accountType, "expense") -> resources.getString(R.string.expense_account)
            Objects.equals(accountType, "revenue") -> resources.getString(R.string.revenue_account)
            Objects.equals(accountType, "liabilities") -> resources.getString(R.string.liability_account)
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
        extendedFab.isGone = true
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }

}