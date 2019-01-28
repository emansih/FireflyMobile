package xyz.hisname.fireflyiii.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GlobalViewModel: ViewModel() {

    val backPress =  MutableLiveData<Boolean>()

    fun handleBackPress(back: Boolean){
        backPress.value = back
    }
}