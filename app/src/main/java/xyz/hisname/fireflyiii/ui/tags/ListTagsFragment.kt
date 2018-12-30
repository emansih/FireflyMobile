package xyz.hisname.fireflyiii.ui.tags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_lists_tags.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.tags.TagsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.toastSuccess

class ListTagsFragment: BaseFragment() {

    private val tagsViewModel by lazy { getViewModel(TagsViewModel::class.java) }
    private lateinit var chipTags: Chip
    private val fab by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.globalFAB) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_lists_tags, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        all_tags.setChipSpacing(16)
        displayView()
        tagsViewModel.apiResponse.observe(this, Observer {
            toastError(it)
        })
        setFab()
        pullToRefresh()
    }

    private fun displayView(){
        tagsViewModel.getAllTags().observe(this, Observer { tags ->
            tagsViewModel.isLoading.observe(this, Observer { isLoading ->
                if(isLoading == false){
                    tags.forEachIndexed { _, tagsData ->
                        chipTags = Chip(requireContext())
                        chipTags.apply {
                            text = tagsData.tagsAttributes?.tag
                            chipIcon = IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_tag)
                                    .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                            isCloseIconVisible = true
                            setOnCloseIconClickListener { close ->
                                val tagName = (close as TextView).text.toString()
                                tagsViewModel.deleteTagByName(tagName).observe(this@ListTagsFragment, Observer { status ->
                                    if (status) {
                                        all_tags.removeAllViews()
                                        toastSuccess("$tagName Deleted")
                                    } else {
                                        toastError("There was an error deleting $tagName", Toast.LENGTH_LONG)
                                    }
                                })
                            }
                        }
                        swipe_tags.isRefreshing = false
                        all_tags.addView(chipTags)
                    }
                } else {
                    swipe_tags.isRefreshing = true
                }
            })
        })
    }

    private fun setFab(){
        fab.apply {
            isVisible = true
            translationY = (6 * 56).toFloat()
            animate().translationY(0.toFloat())
                    .setInterpolator(OvershootInterpolator(1f))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()
            setOnClickListener{
                fab.isClickable = false
                val addTags = AddTagsDialog()
                addTags.arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                addTags.show(requireFragmentManager().beginTransaction(), "add_tags_dialog")
                fab.isClickable = true
            }
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