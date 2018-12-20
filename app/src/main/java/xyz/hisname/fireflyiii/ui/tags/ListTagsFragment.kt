package xyz.hisname.fireflyiii.ui.tags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_lists_tags.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.tags.TagsViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel

class ListTagsFragment: BaseFragment() {

    private val tagsViewModel by lazy { getViewModel(TagsViewModel::class.java) }
    private lateinit var chipTags: Chip

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_lists_tags, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagsViewModel.getAllTags().observe(this, Observer {
            it.forEachIndexed { _, tagsData ->
                chipTags = Chip(requireContext()).apply {
                    text = tagsData.tagsAttributes?.tag
                    chipIcon = IconicsDrawable(requireContext()).
                            icon(FontAwesome.Icon.faw_tag).sizeDp(8)
                            .color(ContextCompat.getColor(requireContext(),R.color.md_green_400))
                }
                all_tags.addView(chipTags)
            }
        })

    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Tags"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Tags"
    }

}