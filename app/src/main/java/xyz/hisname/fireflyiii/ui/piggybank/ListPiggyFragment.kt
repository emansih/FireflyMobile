package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*

class ListPiggyFragment: BaseFragment() {

    private var dataAdapter = arrayListOf<PiggyData>()
    private var whichPiggy = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runLayoutAnimation(recycler_view)
        setHasOptionsMenu(true)
        displayAll()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        swipeContainer.isRefreshing = false
        if (dataAdapter.isNotEmpty()) {
            listText.isVisible = false
            listImage.isVisible = false
            recycler_view.isVisible = true
            recycler_view.adapter =  PiggyRecyclerAdapter(dataAdapter){ data: PiggyData -> itemClicked(data) }.apply {
                update(dataAdapter)
            }
        } else {
            listText.text = resources.getString(R.string.no_piggy_bank)
            listText.isVisible = true
            listImage.isVisible = true
            listImage.setImageDrawable(getCompatDrawable(R.drawable.ic_piggy_bank))
            recycler_view.isVisible = false
        }
    }

    private fun displayAll(){
        swipeContainer.isRefreshing = true
        dataAdapter.clear()
        piggyViewModel.getAllPiggyBanks().observe(this) { piggyBankData ->
            dataAdapter = ArrayList(piggyBankData)
            displayView()
        }
    }

    private fun displayIncomplete(){
        swipeContainer.isRefreshing = true
        dataAdapter.clear()
        piggyViewModel.getNonCompletedPiggyBanks().observe(this) { piggyBankData ->
            dataAdapter = ArrayList(piggyBankData)
            displayView()
        }
    }

    private fun itemClicked(piggyData: PiggyData){
        val bundle = bundleOf("piggyId" to piggyData.piggyId)
        parentFragmentManager.commit {
            replace(R.id.fragment_container, PiggyDetailFragment().apply { arguments = bundle })
            addToBackStack(null)
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            if(whichPiggy){
                displayAll()
            } else {
                displayIncomplete()
            }
        }
    }

    private fun initFab(){
        fab.display {
            fab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddPiggyFragment().apply {
                    arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                })
                addToBackStack(null)
            }
            fab.isClickable = true
        }
        recycler_view.hideFab(fab)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.piggy_bank_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        item.isCheckable = true
        if(id == R.id.menu_incomplete){
            if(item.isChecked){
                item.isChecked = false
                displayAll()
                whichPiggy = true
            } else {
                item.isChecked = true
                displayIncomplete()
                whichPiggy = false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.piggy_bank)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.piggy_bank)
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}