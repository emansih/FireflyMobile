package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.piggy_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*

class ListPiggyFragment: BaseFragment(){

    private val piggyViewModel by lazy { getImprovedViewModel(ListPiggyViewModel::class.java) }
    private val piggyRecyclerAdapter by lazy { PiggyRecyclerAdapter { data: PiggyData -> itemClicked(data) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFab()
        displayView()
        pullToRefresh()
        enableDragDrop()
    }

    private fun displayView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = piggyRecyclerAdapter
        piggyViewModel.getPiggyBank().observe(viewLifecycleOwner){ pagingData ->
            piggyRecyclerAdapter.submitData(lifecycle, pagingData)
        }
        piggyRecyclerAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading) {
                if (piggyRecyclerAdapter.itemCount < 1) {
                    listText.text = resources.getString(R.string.no_piggy_bank)
                    listText.isVisible = true
                    listImage.isVisible = true
                    listImage.setImageDrawable(getCompatDrawable(R.drawable.ic_piggy_bank))
                } else {
                    listImage.isGone = true
                    listText.isGone = true
                }
            }
        }
    }

    private fun itemClicked(piggyData: PiggyData){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, PiggyDetailFragment().apply {
                arguments = bundleOf("piggyId" to piggyData.piggyId)
            })
            addToBackStack(null)
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            piggyRecyclerAdapter.refresh()
        }
    }

    private fun enableDragDrop(){
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.piggyCard.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val piggyName = viewHolder.itemView.piggyName.text.toString()
                    val piggyId = viewHolder.itemView.piggyId.text.toString()
                    piggyViewModel.deletePiggybank(piggyId).observe(viewLifecycleOwner){ isDeleted ->
                        piggyRecyclerAdapter.refresh()
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