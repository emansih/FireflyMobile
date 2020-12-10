package xyz.hisname.fireflyiii.ui.tags

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.commit
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_add_tags.*
import kotlinx.android.synthetic.main.fragment_add_tags.description_edittext
import kotlinx.android.synthetic.main.fragment_add_tags.placeHolderToolbar
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.MapsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*

class AddTagsFragment: BaseAddObjectFragment() {

    private var date: String? = null
    private var description: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var zoomLevel: String? = null
    private val tagId by lazy { arguments?.getLong("tagId") ?: 0 }
    private val addTagViewModel by lazy { getImprovedViewModel(AddTagsViewModel::class.java) }
    private val mapsViewModel by lazy { getViewModel(MapsViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.create(R.layout.fragment_add_tags, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_tags_layout)
        if(tagId != 0L){
            updateData()
        }
        setFab()
    }

    private fun setFab(){
        if(tagId != 0L){
            addTagFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        addTagFab.setOnClickListener {
            hideKeyboard()
            date = if(date_edittext.isBlank()){
                null
            } else {
                date_edittext.getString()
            }
            description = if(description_edittext.isBlank()){
                null
            } else {
                description_edittext.getString()
            }
            latitude = if(latitude_edittext.isBlank()){
                null
            } else {
                latitude_edittext.getString()
            }
            longitude = if(longitude_edittext.isBlank()){
                null
            } else {
                longitude_edittext.getString()
            }
            zoomLevel = if(zoom_edittext.isBlank()){
                null
            } else {
                zoom_edittext.getString()
            }
            if(tagId == 0L){
                submitData()
            } else {
                updateTag()
            }
        }
    }

    private fun updateData(){
        addTagViewModel.getTagById(tagId).observe(viewLifecycleOwner) { tagsData ->
            val tagData = tagsData.tagsAttributes
            tag_edittext.setText(tagData.tag)
            date_edittext.setText(tagData.date)
            description_edittext.setText(tagData.description)
            latitude_edittext.setText(tagData.latitude)
            longitude_edittext.setText(tagData.longitude)
            zoom_edittext.setText(tagData.zoom_level)
        }
    }

    override fun setWidgets(){
        date_edittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(childFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                transaction_date_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        mapTextview.paintFlags = mapTextview.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        mapTextview.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.dialog_add_tags_layout, MapsFragment())
                addToBackStack(null)
            }
            addTagFab.isInvisible = true
        }
        mapsViewModel.latitude.observe(viewLifecycleOwner) {
            if(it != "0.0"){
                latitude_edittext.setText(it)
            }
        }
        mapsViewModel.longitude.observe(viewLifecycleOwner) {
            if(it != "0.0"){
                longitude_edittext.setText(it)
            }
        }
        mapsViewModel.zoomLevel.observe(viewLifecycleOwner) {
            if(it != "0.0"){
                zoom_edittext.setText(it)
            }
        }
        placeHolderToolbar.setOnClickListener {
            handleBack()
        }
        addTagViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
    }

    override fun setIcons(){
        date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        },null, null, null)
        description_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_audio_description
                    colorRes = R.color.md_amber_800
                    sizeDp =24
                },null, null, null)
        latitude_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply{
                    icon = GoogleMaterial.Icon.gmd_map
                    colorRes = R.color.md_green_800
                    sizeDp = 24
                },null, null, null)
        longitude_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply{
                    icon = GoogleMaterial.Icon.gmd_map
                    colorRes = R.color.md_green_800
                    sizeDp = 24
                },null, null, null)
        zoom_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_zoom_in
                    colorRes = R.color.md_black_1000
                    sizeDp = 24
                },null, null, null)
        addTagFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
    }

    override fun submitData(){
        addTagViewModel.addTag(tag_edittext.getString(), date, description, latitude, longitude, zoomLevel).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun updateTag(){
        addTagViewModel.updateTag(tagId, tag_edittext.getString(), date, description, latitude,
                longitude, zoomLevel).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    override fun handleBack() {
        unReveal(dialog_add_tags_layout, true)
    }

}