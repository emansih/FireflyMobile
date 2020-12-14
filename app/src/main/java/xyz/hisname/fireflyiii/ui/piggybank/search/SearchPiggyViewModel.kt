package xyz.hisname.fireflyiii.ui.piggybank.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyPageSource
import xyz.hisname.fireflyiii.repository.piggybank.SearchPiggyPageSource

class SearchPiggyViewModel(application: Application): BaseViewModel(application) {

    private val piggyDao = AppDatabase.getInstance(application).piggyDataDao()
    private val piggyService = genericService().create(PiggybankService::class.java)

    val piggyName = MutableLiveData<String>()

    fun getAllPiggyBank(): LiveData<PagingData<PiggyData>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            PiggyPageSource(piggyDao, piggyService)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }

    fun searchPiggyBank(query: String): LiveData<PagingData<PiggyData>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            SearchPiggyPageSource(piggyDao, query, piggyService)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }
}