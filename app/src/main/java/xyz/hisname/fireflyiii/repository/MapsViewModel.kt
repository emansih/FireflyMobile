package xyz.hisname.fireflyiii.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapsViewModel: ViewModel() {

    val latitude =  MutableLiveData<String>()
    val longitude = MutableLiveData<String>()
    val zoomLevel = MutableLiveData<String>()

    fun setLatitude(lat: Double){
        latitude.value = lat.toString()
    }

    fun setLongitude(longi: Double){
        longitude.value = longi.toString()
    }

    fun setZoomLevel(zoooomLevel: Double){
        zoomLevel.value = zoooomLevel.toString()
    }
}