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
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
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
        tagsViewModel.apiResponse.observe(this, Observer {
            toastError(it)
        })
        setFab()
        pullToRefresh()
    }

    private fun displayView(){
        swipe_tags.isRefreshing = true
        tagsViewModel.getAllTags().observe(this, Observer { tags ->
            tagsViewModel.isLoading.observe(this, Observer { isLoading ->
                if(isLoading == false){
                    if(tags.isEmpty()){
                        listImage.isVisible = true
                        listText.isVisible = true
                        listImage.setImageDrawable(IconicsDrawable(requireContext())
                                .icon(FontAwesome.Icon.faw_tag)
                                .sizeDp(24))
                        listText.text = "No Tags Found! Start tagging now?"
                        swipe_tags.isRefreshing = false
                    } else {
                        listImage.isGone = true
                        listText.isGone = true
                        tags.forEachIndexed { _, tagsData ->
                            chipTags = Chip(requireContext())
                            chipTags.apply {
                                text = tagsData.tagsAttributes?.tag
                                chipIcon = IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_tag)
                                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                                isCloseIconVisible = true
                                setOnCloseIconClickListener { close ->
                                    val tagName = (close as TextView).text.toString()
                                    deleteTag(tagName)
                                }
                                setOnClickListener {
                                    requireFragmentManager().commit {
                                        val addTags = AddTagsFragment()
                                        addTags.arguments = bundleOf("revealX" to fab.width / 2,
                                                "revealY" to fab.height / 2, "tagId" to tagsData.tagsId)
                                        replace(R.id.bigger_fragment_container, addTags)
                                    }
                                }
                            }
                            swipe_tags.isRefreshing = false
                            all_tags.addView(chipTags)
                        }
                    }
                }
            })
        })
    }

    private fun deleteTag(tagName: String){
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_tag_title, tagName))
                .setMessage(resources.getString(R.string.delete_tag_message, tagName))
                .setPositiveButton(R.string.delete_permanently){_, _ ->
                    tagsViewModel.deleteTagByName(tagName).observe(this@ListTagsFragment, Observer { status ->
                        if (status) {
                            requireFragmentManager().commit {
                                replace(R.id.fragment_container, ListTagsFragment())
                            }
                            toastSuccess(resources.getString(R.string.tag_deleted, tagName))
                        } else {
                            toastError("There was an error deleting $tagName", Toast.LENGTH_LONG)
                        }
                    })
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
            requireFragmentManager().commit {
                val addTags = AddTagsFragment()
                addTags.arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                replace(R.id.bigger_fragment_container, addTags)
            }
            fab.isClickable = true
        }
    }

    private fun pullToRefresh(){
        swipe_tags.setOnRefreshListener {
            requireFragmentManager().commit {
                // This hack is so nasty!
                replace(R.id.fragment_container, ListTagsFragment())
            }
        }
        swipe_tags.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
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


}