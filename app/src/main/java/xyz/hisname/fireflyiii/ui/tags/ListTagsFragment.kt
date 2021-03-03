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

package xyz.hisname.fireflyiii.ui.tags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.commit
import com.google.android.material.chip.Chip
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentBaseListBinding
import xyz.hisname.fireflyiii.databinding.FragmentListsTagsBinding
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*

class ListTagsFragment: BaseFragment() {

    private val tagViewModel by lazy { getImprovedViewModel(ListTagsViewModel::class.java) }
    private var fragmentListTagsBinding: FragmentListsTagsBinding? = null
    private val binding get() = fragmentListTagsBinding!!
    private var fragmentBaseBinding: FragmentBaseListBinding? = null
    private val baseListBinding get() = fragmentBaseBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentListTagsBinding = FragmentListsTagsBinding.inflate(inflater, container, false)
        fragmentBaseBinding = binding.tagsBaseList
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseListBinding.baseSwipeLayout.swipeContainer.isGone = true
        binding.allTags.setChipSpacing(16)
        setResponse()
        displayView()
        setFab()
        pullToRefresh()
        binding.tagsNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                extendedFab.hide()
            } else {
                extendedFab.show()
            }
        })
    }

    private fun setResponse(){
        tagViewModel.apiResponse.observe(viewLifecycleOwner) {
            toastError(it)
        }
        tagViewModel.isLoading.observe(viewLifecycleOwner){ isLoading ->
            binding.swipeTags.isRefreshing = isLoading
        }
    }

    private fun displayView(){
        tagViewModel.getAllTags().observe(viewLifecycleOwner) { tags ->
            binding.allTags.removeAllViewsInLayout()
            if(tags.isEmpty()){
                baseListBinding.listImage.isVisible = true
                baseListBinding.listText.isVisible = true
                baseListBinding.listImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_tag
                    sizeDp = 24
                })
                baseListBinding.listText.text = "No Tags Found! Start tagging now?"
            } else {
                baseListBinding.listImage.isGone = true
                baseListBinding.listText.isGone = true
                tags.forEach { tagsData ->
                    val chipTags = Chip(requireContext(), null, R.attr.chipStyle)
                    chipTags.apply {
                        text = tagsData.tagsAttributes.tag
                        isCloseIconVisible = true
                        setOnCloseIconClickListener { close ->
                            val tagName = (close as TextView).text.toString()
                            deleteTag(tagName, chipTags)
                        }
                        setOnClickListener {
                            parentFragmentManager.commit {
                                val tagDetails = TagDetailsFragment()
                                tagDetails.arguments = bundleOf("revealX" to extendedFab.width / 2,
                                        "revealY" to extendedFab.height / 2, "tagId" to tagsData.tagsId)
                                addToBackStack(null)
                                replace(R.id.fragment_container, tagDetails)
                            }
                        }
                    }
                    binding.allTags.addView(chipTags)
                }
            }
        }
    }

    private fun deleteTag(tagName: String, chip: Chip){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_tag_title, tagName))
                .setMessage(resources.getString(R.string.delete_tag_message, tagName))
                .setPositiveButton(R.string.delete_permanently){ _, _ ->
                    tagViewModel.deleteTagByName(tagName).observe(viewLifecycleOwner) { status ->
                        if (status) {
                            chip.setOnCloseIconClickListener(chipClickListener)
                            chip.performCloseIconClick()
                            toastSuccess(resources.getString(R.string.tag_deleted, tagName))
                        } else {
                            toastError("There was an issue deleting your tags")
                        }
                    }
                }
                .setNegativeButton("No"){ _, _ ->
                    toastInfo("Tag not deleted")
                }
                .show()
    }

    private val chipClickListener = View.OnClickListener {
        val anim = AlphaAnimation(1f,0f)
        anim.duration = 250
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation) {
                binding.allTags.removeView(it)
            }
            override fun onAnimationStart(animation: Animation?) {}
        })
        it.startAnimation(anim)
    }


    private fun setFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            requireActivity().findViewById<FrameLayout>(R.id.fragment_container).isInvisible = true
            parentFragmentManager.commit {
                val addTags = AddTagsFragment()
                addTags.arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2)
                addToBackStack(null)
                replace(R.id.bigger_fragment_container, addTags)
            }
            extendedFab.isClickable = true
        }
    }

    private fun pullToRefresh(){
        binding.swipeTags.setOnRefreshListener {
            displayView()
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = resources.getString(R.string.tags)
    }
}