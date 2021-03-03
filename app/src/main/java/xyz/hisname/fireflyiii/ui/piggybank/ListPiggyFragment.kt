/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.BaseSwipeLayoutBinding
import xyz.hisname.fireflyiii.databinding.FragmentBaseListBinding
import xyz.hisname.fireflyiii.databinding.PiggyListItemBinding
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.piggybank.details.PiggyDetailFragment
import xyz.hisname.fireflyiii.util.extension.*

class ListPiggyFragment: BaseFragment(){

    private val piggyViewModel by lazy { getImprovedViewModel(ListPiggyViewModel::class.java) }
    private val piggyRecyclerAdapter by lazy { PiggyRecyclerAdapter { data: PiggyData -> itemClicked(data) } }
    private var fragmentBaseListBinding: FragmentBaseListBinding? = null
    private val binding get() = fragmentBaseListBinding!!
    private var baseSwipeLayoutBinding: BaseSwipeLayoutBinding? = null
    private val baseBinding get() = baseSwipeLayoutBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBaseListBinding = FragmentBaseListBinding.inflate(inflater, container, false)
        baseSwipeLayoutBinding = binding.baseSwipeLayout
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFab()
        displayView()
        pullToRefresh()
        enableDragDrop()
    }

    private fun displayView(){
        baseBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        baseBinding.recyclerView.adapter = piggyRecyclerAdapter
        piggyViewModel.getPiggyBank().observe(viewLifecycleOwner){ pagingData ->
            piggyRecyclerAdapter.submitData(lifecycle, pagingData)
        }
        piggyRecyclerAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            baseBinding.swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading) {
                if (piggyRecyclerAdapter.itemCount < 1) {
                    binding.listText.text = resources.getString(R.string.no_piggy_bank)
                    binding.listText.isVisible = true
                    binding.listImage.isVisible = true
                    binding.listImage.setImageDrawable(getCompatDrawable(R.drawable.ic_piggy_bank))
                } else {
                    binding.listImage.isGone = true
                    binding.listText.isGone = true
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
        baseBinding.swipeContainer.setOnRefreshListener {
            piggyRecyclerAdapter.refresh()
        }
    }

    private fun enableDragDrop(){
        baseBinding.recyclerView.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            val piggyListItemBinding = PiggyListItemBinding.bind(viewHolder.itemView)
            if (piggyListItemBinding.piggyCard.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val piggyName = piggyListItemBinding.piggyName.text.toString()
                    val piggyId = piggyListItemBinding.piggyId.text.toString()
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
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.piggy_bank)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.piggy_bank)
    }
}