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

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentTagDetailsBinding
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File

class TagDetailsFragment: BaseDetailFragment() {

    private val tagId by lazy { arguments?.getLong("tagId") ?: 0 }
    private val nameOfTagFromBundle by lazy { arguments?.getString("tagName") ?: ""}
    private val tagsDetailViewModel by lazy { getImprovedViewModel(TagDetailsViewModel::class.java) }
    private lateinit var nameOfTag: String
    private lateinit var startMarker: Marker
    private var fragmentTagDetailsBinding: FragmentTagDetailsBinding? = null
    private val binding get() = fragmentTagDetailsBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentTagDetailsBinding = FragmentTagDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireContext().filesDir
        Configuration.getInstance().osmdroidTileCache = File(requireContext().filesDir.toString() + "/tiles")
        if(tagId != 0L) {
            tagsDetailViewModel.getTagById(tagId).observe(viewLifecycleOwner) { data ->
                setTagData(data)
            }
        } else {
            tagsDetailViewModel.getTagByName(nameOfTagFromBundle).observe(viewLifecycleOwner) { data ->
                setTagData(data)
            }
        }
        tagsDetailViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun setTagData(tagData: TagsData){
        binding.tagName.chipIcon = IconicsDrawable(requireContext()).apply{
            icon = FontAwesome.Icon.faw_tag
            colorRes = R.color.md_green_400
        }
        val tagsAttributes = tagData.tagsAttributes
        binding.tagName.text = tagsAttributes.tag
        nameOfTag = tagsAttributes.tag
        if(tagsAttributes.latitude.isEmpty() ||
                tagsAttributes.longitude.isEmpty() || tagsAttributes.zoom_level.isEmpty()){
            binding.latitudeText.text = resources.getString(R.string.no_location_set)
            binding.longitudeText.text = resources.getString(R.string.no_location_set)
            binding.zoomText.text = resources.getString(R.string.no_location_set)
            binding.tagDetailsMapText.isVisible = true
            binding.tagDetailsMap.isInvisible = true
        } else {
            binding.tagDetailsMap.isVisible = true
            startMarker = Marker(binding.tagDetailsMap)
            binding.tagDetailsMap.setTileSource(TileSourceFactory.MAPNIK)
            binding.tagDetailsMap.setMultiTouchControls(true)
            binding.tagDetailsMap.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            binding.tagDetailsMap.overlays.add(startMarker)
            startMarker.icon = IconicsDrawable(requireContext()).apply{
                icon = FontAwesome.Icon.faw_map_marker
                colorRes = R.color.md_red_700
                sizeDp = 16
            }
            binding.latitudeText.text = tagsAttributes.latitude
            binding.longitudeText.text = tagsAttributes.longitude
            val userCoord = GeoPoint(tagsAttributes.latitude.toDouble(),
                    tagsAttributes.longitude.toDouble())
            startMarker.position = userCoord
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            binding.tagDetailsMap.controller.animateTo(userCoord)
            binding.tagDetailsMap.controller.setZoom(tagsAttributes.zoom_level.toDouble())
            binding.zoomText.text = tagsAttributes.zoom_level
        }
        binding.tagDescription.text = tagsAttributes.description
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
                    tagsDetailViewModel.deleteTagByName(nameOfTag).observe(viewLifecycleOwner) { status ->
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
}