package xyz.hisname.fireflyiii.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DateRangeViewModel: ViewModel() {

    val startDate = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()

    fun startDate(start: String){
        startDate.value = start
    }

    fun endDate(end: String){
        endDate.value = end
    }
}
