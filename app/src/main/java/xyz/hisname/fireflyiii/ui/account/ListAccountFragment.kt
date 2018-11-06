package xyz.hisname.fireflyiii.ui.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.viewmodel.AccountsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import java.util.*

class ListAccountFragment: BaseFragment() {

    private var dataAdapter = ArrayList<AccountData>()
    private val fab by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.globalFAB) }
    private val accountViewModel by lazy {
        getViewModel(AccountsViewModel::class.java).getAccountsByType(baseUrl, accessToken, accountType)}
    private val accountType by lazy { arguments?.getString("accountType") ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.base_swipe_layout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        swipeContainer.isRefreshing = true
        runLayoutAnimation(recycler_view)
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        accountViewModel.databaseData?.observe(
                this, Observer {
            swipeContainer.isRefreshing = false
            if(it.isNotEmpty()){
                recycler_view.adapter = AccountRecyclerAdapter(it) { data: AccountData -> itemClicked(data) }
            }
        })
        accountViewModel.apiResponse
                .observe(this, Observer {
                    swipeContainer.isRefreshing = false
                    if(it.getError() != null){
                        toastError(it.getError()?.message)
                    }
        })
    }

    private fun itemClicked(data: AccountData){
        val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken,
                "accountId" to data.accountId)
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
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
    }

    private fun initFab(){
        fab.apply {
            isVisible = true
            translationY = (6 * 56).toFloat()
            animate().translationY(0f)
                    .setInterpolator(OvershootInterpolator(1f))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()
            setOnClickListener {
                val bundle = bundleOf("fireflyUrl" to baseUrl,
                        "access_token" to accessToken)
                requireFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,
                                AddAccountFragment().apply { arguments = bundle })
                        .addToBackStack(null)
                        .commit()
                requireActivity().globalFAB.isVisible = false
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0 && fab.isShown){
                    fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun convertString(): String{
        if(Objects.equals(accountType, "asset")){
            return "Asset Account"
        } else if(Objects.equals(accountType, "expense")){
            return "Expense Account"
        } else if(Objects.equals(accountType, "revenue")){
            return "Revenue Account"
        } else {
            return "Accounts"
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

    override fun onStop() {
        super.onStop()
        fab.isGone = true
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }

}