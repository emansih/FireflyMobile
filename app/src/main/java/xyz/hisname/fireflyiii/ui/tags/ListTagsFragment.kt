package xyz.hisname.fireflyiii.ui.tags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.google.android.material.chip.Chip
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.fragment_lists_tags.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*

class ListTagsFragment: BaseFragment() {

    private lateinit var chipTags: Chip
    private val baseLayout by lazy { requireActivity().findViewById<FrameLayout>(R.id.fragment_container) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_lists_tags, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseSwipeLayout.isGone = true
        all_tags.setChipSpacing(16)
        displayView()
        tagsViewModel.apiResponse.observe(viewLifecycleOwner) {
            toastError(it)
        }
        setFab()
        pullToRefresh()
    }

    private fun displayView(){
        swipe_tags.isRefreshing = true
        tagsViewModel.getAllTags().observe(viewLifecycleOwner) { tags ->
            all_tags.removeAllViewsInLayout()
            tagsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if(isLoading == false){
                    if(tags.isEmpty()){
                        listImage.isVisible = true
                        listText.isVisible = true
                        listImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                            icon = FontAwesome.Icon.faw_tag
                            sizeDp = 24
                        })
                        listText.text = "No Tags Found! Start tagging now?"
                        swipe_tags.isRefreshing = false
                    } else {
                        listImage.isGone = true
                        listText.isGone = true
                        tags.forEachIndexed { _, tagsData ->
                            chipTags = Chip(requireContext())
                            chipTags.apply {
                                text = tagsData.tagsAttributes?.tag
                                chipIcon = IconicsDrawable(requireContext()).apply{
                                    icon = FontAwesome.Icon.faw_tag
                                    colorRes = R.color.md_green_400
                                }
                                isCloseIconVisible = true
                                addColor()
                                setOnCloseIconClickListener { close ->
                                    val tagName = (close as TextView).text.toString()
                                    deleteTag(tagName)
                                }
                                setOnClickListener {
                                    parentFragmentManager.commit {
                                        val tagDetails = TagDetailsFragment()
                                        tagDetails.arguments = bundleOf("revealX" to fab.width / 2,
                                                "revealY" to fab.height / 2, "tagId" to tagsData.tagsId)
                                        addToBackStack(null)
                                        replace(R.id.fragment_container, tagDetails)
                                    }
                                }
                            }
                            swipe_tags.isRefreshing = false
                            all_tags.addView(chipTags)
                        }
                    }
                }
            }
        }
    }

    private fun deleteTag(tagName: String){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_tag_title, tagName))
                .setMessage(resources.getString(R.string.delete_tag_message, tagName))
                .setPositiveButton(R.string.delete_permanently){_, _ ->
                    tagsViewModel.deleteTagByName(tagName).observe(viewLifecycleOwner) { status ->
                        if (status) {
                            parentFragmentManager.commit {
                                replace(R.id.fragment_container, ListTagsFragment())
                            }
                            toastSuccess(resources.getString(R.string.tag_deleted, tagName))
                        } else {
                            toastError("There was an error deleting $tagName", Toast.LENGTH_LONG)
                        }
                    }
                }
                .setNegativeButton("No"){ _, _ ->
                    toastInfo("Tag not deleted")
                }
                .show()
    }

    private fun setFab(){
        fab.display {
            fab.isClickable = false
            baseLayout.isInvisible = true
            parentFragmentManager.commit {
                val addTags = AddTagsFragment()
                addTags.arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                addToBackStack(null)
                replace(R.id.bigger_fragment_container, addTags)
            }
            fab.isClickable = true
        }
    }

    private fun pullToRefresh(){
        swipe_tags.setOnRefreshListener {
            parentFragmentManager.commit {
                // This hack is so nasty!
                replace(R.id.fragment_container, ListTagsFragment())
            }
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.tags)
    }

    override fun onStop() {
        super.onStop()
        fab.isGone = true
        activity?.activity_toolbar?.title = resources.getString(R.string.tags)
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.tags)
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }

}