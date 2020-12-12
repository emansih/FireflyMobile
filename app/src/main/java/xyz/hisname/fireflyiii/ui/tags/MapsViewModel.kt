package xyz.hisname.fireflyiii.ui.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.remote.nominatim.NominatimClient
import xyz.hisname.fireflyiii.repository.NominatimRepository

class MapsViewModel: ViewModel() {


    val latitude =  MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val zoomLevel = MutableLiveData<Double>()


    fun getLocationFromQuery(query: String): LiveData<List<String>>{
        val locationList = MutableLiveData<List<String>>()
        viewModelScope.launch(Dispatchers.IO){
            locationList.postValue(NominatimRepository().getLocationFromQuery(query))
        }
        return locationList
    }

    override fun onCleared() {
        super.onCleared()
        NominatimClient.destroyClient()
    }
}