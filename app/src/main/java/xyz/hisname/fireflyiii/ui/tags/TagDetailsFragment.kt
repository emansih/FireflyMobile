package xyz.hisname.fireflyiii.ui.tags

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_tag_details.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.util.extension.*

class TagDetailsFragment: BaseDetailFragment() {

    private val tagId by lazy { arguments?.getLong("tagId") ?: 0 }
    private lateinit var nameOfTag: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        fab.isGone = true
        return inflater.create(R.layout.fragment_tag_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val startMarker = Marker(tagDetailsMap)
        tagDetailsMap.setTileSource(TileSourceFactory.MAPNIK)
        tagDetailsMap.setMultiTouchControls(true)
        tagDetailsMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        tagDetailsMap.overlays.add(startMarker)
        startMarker.icon = IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_map_marker)
                .color(ContextCompat.getColor(requireContext(), R.color.md_red_700))
                .sizeDp(16)
        tagsViewModel.getTagById(tagId).observe(this, Observer {
            val tagsAttributes = it[0].tagsAttributes
            if(tagsAttributes?.tag != null){
                tagName.text = tagsAttributes.tag
                nameOfTag = tagsAttributes.tag
            }
            if(tagsAttributes?.latitude == null || tagsAttributes.longitude == null || tagsAttributes.zoom_level == null){
                latitude_text.text = "No location set"
                longitude_text.text = "No location set"
                zoom_text.text = "No location set"
                val groomLake = GeoPoint(37.276675, -115.798936)
                startMarker.position = groomLake
                tagDetailsMap.controller.animateTo(groomLake)
                tagDetailsMap.controller.setZoom(15.0)
            } else {
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
            tagDescription.text = tagsAttributes?.description
        })
    }

    override fun onStop() {
        super.onStop()
        fab.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.menu_item_edit -> consume {
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, AddTagsFragment().apply {
                    arguments = bundleOf("tagId" to tagId)
                })
                addToBackStack(null)
            }
        }
        R.id.menu_item_delete -> consume {
            deleteItem()
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun deleteItem() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_tag_title, nameOfTag))
                .setMessage(resources.getString(R.string.delete_tag_message, nameOfTag))
                .setPositiveButton(R.string.delete_permanently){_, _ ->
                    ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
                    tagsViewModel.deleteTagByName(nameOfTag).observe(this@TagDetailsFragment, Observer { status ->
                        ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                        if (status) {
                            requireFragmentManager().commit {
                                replace(R.id.fragment_container, ListTagsFragment())
                            }
                            toastSuccess(resources.getString(R.string.tag_deleted, nameOfTag))
                        } else {
                            toastError("There was an error deleting $nameOfTag", Toast.LENGTH_LONG)
                        }
                    })
                }
                .setNegativeButton("No"){ _, _ ->
                    toastInfo("Tag not deleted")
                }
                .show()
    }

}