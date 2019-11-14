package xyz.hisname.fireflyiii.ui.tags

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.*
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.nominatim.LocationSearchModel
import xyz.hisname.fireflyiii.repository.nominatim.NominatimViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.io.File

class MapsFragment: BaseFragment() {

    private val locationService by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val nominatimViewModel by lazy { getViewModel(NominatimViewModel::class.java) }
    private val mapController by lazy { maps.controller }
    private val groomLake by lazy { GeoPoint(37.276675, -115.798936) }
    private lateinit var startMarker: Marker
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var cloneLocationList: List<LocationSearchModel>

    companion object {
        private const val PERMISSION_LOCATION_REQUEST = 123
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireContext().filesDir
        Configuration.getInstance().osmdroidTileCache = File(requireContext().filesDir.toString() + "/tiles")
        return inflater.create(R.layout.fragment_map, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isGpsEnabled()
        setMap()
        setMapClick()
        setFab()
        searchLocation()
        okButton.setOnClickListener {
            mapsViewModel.setLatitude(latitude)
            mapsViewModel.setLongitude(longitude)
            mapsViewModel.setZoomLevel(maps.zoomLevelDouble)
            handleBack()
        }
        cancelButton.setOnClickListener {
            mapsViewModel.setLatitude(0.0)
            mapsViewModel.setLongitude(0.0)
            mapsViewModel.setZoomLevel(0.0)
            handleBack()
        }
    }

    private fun setMap(){
        startMarker = Marker(maps)
        startMarker.position = groomLake
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        maps.setMultiTouchControls(true)
        maps.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        maps.overlays.add(startMarker)
        maps.setTileSource(TileSourceFactory.MAPNIK)
        startMarker.icon = IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_map_marker)
                .colorInt(R.color.md_red_700)
                .sizeDp(16)
        mapController.animateTo(groomLake)
        mapController.setZoom(15.0)
    }

    private fun searchLocation(){
        mapSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(editable: Editable) {
                location(editable.toString())
            }

            override fun beforeTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {
            }

        })
        mapSearch.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val startPoint = GeoPoint(cloneLocationList[i].lat, cloneLocationList[i].lon)
            hideKeyboard()
            startMarker.position = startPoint
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapController.setZoom(15.0)
            mapController.animateTo(startPoint)
            longitude = cloneLocationList[i].lon
            latitude = cloneLocationList[i].lat
        }
    }

    private fun location(query: String){
        nominatimViewModel.getLocationFromQuery(query).observe(this@MapsFragment){ locationList ->
            val displayName = arrayListOf<String>()
            cloneLocationList = locationList
            locationList.forEachIndexed { _, locationSearchModel ->
                displayName.add(locationSearchModel.display_name)
            }
            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item, displayName)
            mapSearch.threshold = 3
            mapSearch.setAdapter(adapter)
        }
    }

    private fun setMapClick(){
        val mapReceiver = object : MapEventsReceiver{
            override fun longPressHelper(geoPoint: GeoPoint): Boolean {
                startMarker.position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                longitude = geoPoint.longitude
                latitude = geoPoint.latitude
                return true
            }

            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                startMarker.position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                longitude = geoPoint.longitude
                latitude = geoPoint.latitude
                return true
            }

        }
        val overlayEvents = MapEventsOverlay(mapReceiver)
        maps.overlays.add(overlayEvents)
    }

    private fun setFab(){
        fab_map.setImageDrawable(IconicsDrawable(requireContext())
                .icon(GoogleMaterial.Icon.gmd_my_location)
                .colorInt(R.color.md_black_1000)
                .sizeDp(16))
        fab_map.setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                                Manifest.permission.ACCESS_FINE_LOCATION)){
                    AlertDialog.Builder(requireActivity())
                            .setTitle("Grant access to location data?")
                            .setMessage("Choosing coordinates data is simple when location data permission is granted. " +
                                    "Otherwise you may have to manually search for your location")
                            .setPositiveButton("OK"){_,_ ->
                                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_REQUEST)
                            }
                            .setNegativeButton("No"){ _,_ ->
                                toastInfo("Alright...")
                            }
                            .show()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_REQUEST)
                }
            } else {
                locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            mapController.setZoom(18.0)
            val startPoint = GeoPoint(location.latitude, location.longitude)
            mapController.animateTo(startPoint)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun isGpsEnabled(){
        if(!locationService.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder(requireActivity())
                    .setMessage("For a better experience turn on device's location")
                    .setPositiveButton("Sure"){_, _ ->
                        requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setNegativeButton("No"){ _, _ ->
                        toastInfo("Alright...Using Network data instead.")
                    }
                    .show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_LOCATION_REQUEST -> {
                if (grantResults.size == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                    if (ContextCompat.checkSelfPermission(requireContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        toastInfo("Waiting for location...")
                        locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                        locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        maps.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
            locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        }
    }

    override fun onPause() {
        super.onPause()
        maps.onPause()
        if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationService.removeUpdates(locationListener)
        }
    }

    override fun handleBack() {
        requireParentFragment().parentFragmentManager.popBackStack()
        requireActivity().findViewById<FloatingActionButton>(R.id.addTagFab).isVisible = true
    }
}