package xyz.hisname.fireflyiii.repository

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import kotlin.coroutines.CoroutineContext

open class BaseViewModel(application: Application) : AndroidViewModel(application){

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    val scope = CoroutineScope(coroutineContext)
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val apiResponse: MutableLiveData<String> = MutableLiveData()

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}