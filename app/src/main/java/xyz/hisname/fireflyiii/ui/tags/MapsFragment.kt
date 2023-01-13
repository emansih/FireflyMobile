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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentMapBinding
import xyz.hisname.fireflyiii.repository.models.nominatim.LocationSearchModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.hideKeyboard
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.getUniqueHash
import xyz.hisname.fireflyiii.util.isPermissionGranted
import xyz.hisname.fireflyiii.util.launchLocationPermissionsRequest
import java.io.File

class MapsFragment : BaseFragment() {

    private val locationService by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mapsViewModel by lazy { getViewModel(MapsViewModel::class.java) }
    private val longitudeBundle by lazy { arguments?.getString("longitude") }
    private val latitudeBundle by lazy { arguments?.getString("latitude") }
    private lateinit var startMarker: Marker
    private lateinit var cloneLocationList: List<LocationSearchModel>
    private lateinit var gpsPermission: ActivityResultLauncher<Array<String>>
    private var fragmentMapBinding: FragmentMapBinding? = null
    private val binding get() = fragmentMapBinding!!
    private val mapController by lazy { binding.maps.controller }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMapBinding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { success ->
            if (success.any()) {
                if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    toastInfo("Waiting for location...")
                    locationService.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L,
                        0f,
                        locationListener,
                    )
                    locationService.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0L,
                        0f,
                        locationListener,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Configuration.getInstance().load(
            requireContext(),
            requireContext()
                .getSharedPreferences(
                    requireContext().getUniqueHash() + "-user-preferences",
                    Context.MODE_PRIVATE
                )
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireContext().filesDir
        Configuration.getInstance().osmdroidTileCache =
            File(requireContext().filesDir.toString() + "/tiles")
        startMarker = Marker(binding.maps)
        if (!latitudeBundle.isNullOrEmpty() && !longitudeBundle.isNullOrEmpty()) {
            setMap(
                GeoPoint(
                    latitudeBundle?.toDouble() ?: 37.276675,
                    longitudeBundle?.toDouble() ?: -115.798936
                )
            )
        } else {
            isGpsEnabled()
        }
        setMapClick()
        setFab()
        searchLocation()
        binding.okButton.setOnClickListener {
            mapsViewModel.latitude.postValue(startMarker.position.latitude)
            mapsViewModel.longitude.postValue(startMarker.position.longitude)
            mapsViewModel.zoomLevel.postValue(binding.maps.zoomLevelDouble)
            parentFragmentManager.popBackStack()
        }
        binding.cancelButton.setOnClickListener {
            mapsViewModel.latitude.postValue(0.0)
            mapsViewModel.longitude.postValue(0.0)
            mapsViewModel.zoomLevel.postValue(0.0)
            parentFragmentManager.popBackStack()
        }
    }

    private fun setMap(location: GeoPoint) {
        startMarker.position = location
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.maps.setMultiTouchControls(true)
        binding.maps.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        binding.maps.overlays.add(startMarker)
        binding.maps.setTileSource(TileSourceFactory.MAPNIK)
        startMarker.icon = IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_map_marker
            colorRes = R.color.md_red_700
            sizeDp = 16
        }
        mapController.animateTo(location)
        mapController.setZoom(18.0)
    }

    private fun searchLocation() {
        binding.mapSearch.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                location(binding.mapSearch.text.toString())
                hideKeyboard()
            }
            false
        }
        binding.mapSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotBlank()) {
                    location(editable.toString())
                }
            }

            override fun beforeTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {}

        })
        binding.mapSearch.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            setMap(GeoPoint(cloneLocationList[i].lat, cloneLocationList[i].lon))
        }
    }

    private fun location(query: String) {
        mapsViewModel.getLocationFromQuery(query).observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                val adapter =
                    ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, data)
                binding.mapSearch.setAdapter(adapter)
            }
        }
    }

    private fun setMapClick() {
        val mapReceiver = object : MapEventsReceiver {
            override fun longPressHelper(geoPoint: GeoPoint): Boolean {
                setMap(geoPoint)
                return true
            }

            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                setMap(geoPoint)
                return true
            }
        }
        binding.maps.overlays.add(MapEventsOverlay(mapReceiver))
    }

    private fun setFab() {
        binding.fabMap.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = GoogleMaterial.Icon.gmd_my_location
            colorRes = R.color.md_black_1000
            sizeDp = 16
        })
        binding.fabMap.setOnClickListener {
            if (!isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    AlertDialog.Builder(requireActivity())
                        .setTitle("Grant access to location data?")
                        .setMessage(
                            "Choosing coordinates data is simple when location data permission is granted. " +
                                    "Otherwise you may have to manually search for your location"
                        )
                        .setPositiveButton("OK") { _, _ ->
                            gpsPermission.launchLocationPermissionsRequest()
                        }
                        .setNegativeButton("No") { _, _ ->
                            toastInfo("Alright...")
                        }
                        .show()
                } else
                    gpsPermission.launchLocationPermissionsRequest()
            } else {
                locationService.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
                locationService.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            }
        }
    }

    private val locationListener: LocationListener = LocationListener { location ->
        setMap(GeoPoint(location.latitude, location.longitude))
    }

    private fun isGpsEnabled() {
        if (!locationService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(requireActivity())
                .setMessage("For a better experience turn on device's location")
                .setPositiveButton("Sure") { _, _ ->
                    requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("No") { _, _ ->
                    toastInfo("Alright...Using Network data instead.")
                }
                .show()
        } else {
            if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                toastInfo("Acquiring current location...")
                locationService.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
                locationService.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            } else {
                setMap(GeoPoint(37.276675, -115.798936))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.maps.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.maps.onPause()
        if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            locationService.removeUpdates(locationListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().viewModelStore.clear()
    }
}