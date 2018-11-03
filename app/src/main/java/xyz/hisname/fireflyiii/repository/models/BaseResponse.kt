package xyz.hisname.fireflyiii.repository.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

data class BaseResponse<T, H>(val databaseData: LiveData<MutableList<T>>?, val apiResponse: MediatorLiveData<H>)
data class Response<T,H>(val databaseData: MutableList<T>?, val apiResponse: MediatorLiveData<H>)