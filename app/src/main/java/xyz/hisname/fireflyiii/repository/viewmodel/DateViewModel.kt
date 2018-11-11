package xyz.hisname.fireflyiii.repository.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DateViewModel: ViewModel(){

    val startDate =  MutableLiveData<String>()
    val endDate = MutableLiveData<String>()

    fun setDateRange(start: String, end: String){
        startDate.value = start
        endDate.value = end
    }

    override fun onCleared() {
        super.onCleared()
        startDate.value = null
        endDate.value = null
    }
}