package xyz.hisname.fireflyiii.repository.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GlobalViewModel: ViewModel(){

    val baseUrl =  MutableLiveData<String>()
    val accessToken = MutableLiveData<String>()

    fun setUrl(url: String){
        baseUrl.value = url
    }

    fun setToken(token: String){
        accessToken.value = token
    }
}