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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_map.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.*
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.MapsViewModel
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo

class MapsFragment: DialogFragment() {

    private val locationService by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mapController by lazy { maps.controller }
    private lateinit var startMarker: Marker
    private val groomLake by lazy { GeoPoint(37.276675, -115.798936) }
    private val mapsViewModel by lazy { getViewModel(MapsViewModel::class.java) }
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    companion object {
        private const val PERMISSION_LOCATION_REQUEST = 123
        private const val PERMISSION_STORAGE_REQUEST = 321
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        return inflater.create(R.layout.fragment_map, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isGpsEnabled()
        setMap()
        setMapClick()
        setFab()
        okButton.setOnClickListener {
            mapsViewModel.setLatitude(latitude)
            mapsViewModel.setLongitude(longitude)
            mapsViewModel.setZoomLevel(maps.zoomLevelDouble)
            dialog?.dismiss()
        }
        cancelButton.setOnClickListener {
            mapsViewModel.setLatitude(0.0)
            mapsViewModel.setLongitude(0.0)
            mapsViewModel.setZoomLevel(0.0)
            dialog?.dismiss()
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
                .color(ContextCompat.getColor(requireContext(), R.color.md_red_700))
                .sizeDp(16)
        mapController.animateTo(groomLake)
        mapController.setZoom(15.0)
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
                .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
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
                                askStoragePerm()
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

    private fun askStoragePerm(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(requireActivity())
                        .setTitle("Would you like to cache location data?")
                        .setMessage("This allows map data to initialize faster")
                        .setPositiveButton("OK") { _, _ ->
                            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE_REQUEST)
                        }
                        .setNegativeButton("No") { _, _ ->
                        }
                        .show()
            } else {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE_REQUEST)

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
                        askStoragePerm()
                    }
                }
            }
            PERMISSION_STORAGE_REQUEST -> {
                if (grantResults.size == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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
}