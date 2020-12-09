package xyz.hisname.fireflyiii.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.remote.firefly.api.DataService

class DestroyItemsViewModel(application: Application): BaseViewModel(application)   {

    val message: MutableLiveData<String> = MutableLiveData()

    fun deleteObject(objectToDelete: String){
        viewModelScope.launch(Dispatchers.IO) {
            val networkCall = genericService().create(DataService::class.java).destroyItem(objectToDelete)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.code() == 204){
                message.postValue("Deleted")
            } else {
                message.postValue("There was an issue deleting")
            }
        }
    }
}