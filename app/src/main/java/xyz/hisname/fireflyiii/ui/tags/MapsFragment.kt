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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
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
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.io.File

class MapsFragment: BaseFragment() {

    private val locationService by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mapsViewModel by lazy { getViewModel(MapsViewModel::class.java) }
    private val mapController by lazy { maps.controller }
    private lateinit var startMarker: Marker
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var cloneLocationList: List<LocationSearchModel>
    private lateinit var gpsPermission: ActivityResultLauncher<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.create(R.layout.fragment_map, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { success ->
            if(success) {
                if (ContextCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    toastInfo("Waiting for location...")
                    locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                    locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireContext().filesDir
        Configuration.getInstance().osmdroidTileCache = File(requireContext().filesDir.toString() + "/tiles")
        startMarker = Marker(maps)
        setMapClick()
        isGpsEnabled()
        setFab()
        searchLocation()
        okButton.setOnClickListener {
            mapsViewModel.latitude.postValue(latitude)
            mapsViewModel.longitude.postValue(longitude)
            mapsViewModel.zoomLevel.postValue(maps.zoomLevelDouble)
            handleBack()
        }
        cancelButton.setOnClickListener {
            mapsViewModel.latitude.postValue(0.0)
            mapsViewModel.longitude.postValue(0.0)
            mapsViewModel.zoomLevel.postValue(0.0)
            handleBack()
        }
    }

    private fun setMap(location: GeoPoint){
        startMarker.position = location
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        maps.setMultiTouchControls(true)
        maps.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        maps.overlays.add(startMarker)
        maps.setTileSource(TileSourceFactory.MAPNIK)
        startMarker.icon = IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_map_marker
            colorRes = R.color.md_red_700
            sizeDp = 16
        }
        mapController.animateTo(location)
        mapController.setZoom(18.0)
    }

    private fun searchLocation(){
        mapSearch.setOnKeyListener { v, keyCode, event ->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                location(mapSearch.text.toString())
                hideKeyboard()
            }
            false
        }
        mapSearch.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(editable: Editable) {
                if(editable.isNotBlank()) {
                    location(editable.toString())
                }
            }

            override fun beforeTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {}

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
        mapsViewModel.getLocationFromQuery(query).observe(viewLifecycleOwner){ data ->
            if(data.isNotEmpty()){
                val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, data)
                mapSearch.setAdapter(adapter)
            }
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
        maps.overlays.add(MapEventsOverlay(mapReceiver))
    }

    private fun setFab(){
        fab_map.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_my_location
            colorRes = R.color.md_black_1000
            sizeDp = 16
        })
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
                                gpsPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            .setNegativeButton("No"){ _,_ ->
                                toastInfo("Alright...")
                            }
                            .show()
                } else {
                    gpsPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            setMap(GeoPoint(location.latitude, location.longitude))
            latitude = location.latitude
            longitude = location.longitude
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
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
                locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            } else {
                setMap(GeoPoint(37.276675, -115.798936))
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
        parentFragmentManager.popBackStack()
    }

}