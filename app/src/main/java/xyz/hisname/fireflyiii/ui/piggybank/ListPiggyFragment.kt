package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.piggy_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*

class ListPiggyFragment: BaseFragment(){

    private var dataAdapter = arrayListOf<PiggyData>()
    private var whichPiggy = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runLayoutAnimation(recycler_view)
        initFab()
        setHasOptionsMenu(true)
        displayAll()
        pullToRefresh()
        enableDragDrop()
    }

    private fun displayView(){
        swipeContainer.isRefreshing = false
        if (dataAdapter.isNotEmpty()) {
            listText.isVisible = false
            listImage.isVisible = false
            recycler_view.isVisible = true
            recycler_view.adapter =  PiggyRecyclerAdapter(dataAdapter){ data: PiggyData ->
                itemClicked(data)
            }.apply {
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
        piggyViewModel.getAllPiggyBanks().observe(viewLifecycleOwner) { piggyBankData ->
            dataAdapter = ArrayList(piggyBankData)
            displayView()
        }
    }

    private fun displayIncomplete(){
        swipeContainer.isRefreshing = true
        dataAdapter.clear()
        piggyViewModel.getNonCompletedPiggyBanks().observe(viewLifecycleOwner) { piggyBankData ->
            dataAdapter = ArrayList(piggyBankData)
            displayView()
        }
    }

    private fun itemClicked(piggyData: PiggyData){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, PiggyDetailFragment().apply {
                arguments = bundleOf("piggyId" to piggyData.piggyId)
            })
            addToBackStack(null)
        }
        extendedFab.isGone = true
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

    private fun enableDragDrop(){
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.piggyCard.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val piggyName = viewHolder.itemView.piggyName.text.toString()
                    piggyViewModel.deletePiggyByName(piggyName).observe(viewLifecycleOwner){ isDeleted ->
                        if(isDeleted){
                            toastSuccess(resources.getString(R.string.piggy_bank_deleted, piggyName))
                        } else {
                            toastOffline(resources.getString(R.string.data_will_be_deleted_later, piggyName), Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddPiggyFragment().apply {
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
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

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}