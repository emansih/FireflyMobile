package xyz.hisname.fireflyiii.ui.tags

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_tag_details.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.util.extension.*

class TagDetailsFragment: BaseDetailFragment() {

    private val tagId by lazy { arguments?.getLong("tagId") ?: 0 }
    private val nameOfTagFromBundle by lazy { arguments?.getString("tagName") ?: ""}
    private lateinit var nameOfTag: String
    private lateinit var startMarker: Marker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        extendedFab.isGone = true
        return inflater.create(R.layout.fragment_tag_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(tagId != 0L) {
            tagsViewModel.getTagById(tagId).observe(viewLifecycleOwner) { data ->
                setTagData(data)
            }
        } else {
            tagsViewModel.getTagByName(nameOfTagFromBundle).observe(viewLifecycleOwner) { data ->
                setTagData(data)
            }
        }

    }

    private fun setTagData(tagData: TagsData){
        tagName.chipIcon = IconicsDrawable(requireContext()).apply{
            icon = FontAwesome.Icon.faw_tag
            colorRes = R.color.md_green_400
        }
        val tagsAttributes = tagData.tagsAttributes
        tagName.text = tagsAttributes.tag
        nameOfTag = tagsAttributes.tag
        if(tagsAttributes.latitude.isEmpty() ||
                tagsAttributes.longitude.isEmpty() || tagsAttributes.zoom_level.isEmpty()){
            latitude_text.text = resources.getString(R.string.no_location_set)
            longitude_text.text = resources.getString(R.string.no_location_set)
            zoom_text.text = resources.getString(R.string.no_location_set)
            tagDetailsMapText.isVisible = true
            tagDetailsMap.isInvisible = true
        } else {
            tagDetailsMap.isVisible = true
            startMarker = Marker(tagDetailsMap)
            tagDetailsMap.setTileSource(TileSourceFactory.MAPNIK)
            tagDetailsMap.setMultiTouchControls(true)
            tagDetailsMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            tagDetailsMap.overlays.add(startMarker)
            startMarker.icon = IconicsDrawable(requireContext()).apply{
                icon = FontAwesome.Icon.faw_map_marker
                colorRes = R.color.md_red_700
                sizeDp = 16
            }
            latitude_text.text = tagsAttributes.latitude
            longitude_text.text = tagsAttributes.longitude
            val userCoord = GeoPoint(tagsAttributes.latitude.toDouble(),
                    tagsAttributes.longitude.toDouble())
            startMarker.position = userCoord
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            tagDetailsMap.controller.animateTo(userCoord)
            tagDetailsMap.controller.setZoom(tagsAttributes.zoom_level.toDouble())
            zoom_text.text = tagsAttributes.zoom_level
        }
        tagDescription.text = tagsAttributes.description
    }

    override fun onStop() {
        super.onStop()
        extendedFab.isVisible = true
    }

    override fun editItem() {
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddTagsFragment().apply {
                arguments = bundleOf("tagId" to tagId)
            })
            addToBackStack(null)
        }
    }

    override fun deleteItem() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_tag_title, nameOfTag))
                .setMessage(resources.getString(R.string.delete_tag_message, nameOfTag))
                .setPositiveButton(R.string.delete_permanently){_, _ ->
                    ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
                    tagsViewModel.deleteTagByName(nameOfTag).observe(viewLifecycleOwner) { status ->
                        ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                        if (status) {
                            parentFragmentManager.commit {
                                replace(R.id.fragment_container, ListTagsFragment())
                            }
                            toastSuccess(resources.getString(R.string.tag_deleted, nameOfTag))
                        } else {
                            toastError("There was an error deleting $nameOfTag", Toast.LENGTH_LONG)
                        }
                    }
                }
                .setNegativeButton(resources.getString(android.R.string.no)){ _, _ ->
                    toastInfo("Tag not deleted")
                }
                .show()
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}