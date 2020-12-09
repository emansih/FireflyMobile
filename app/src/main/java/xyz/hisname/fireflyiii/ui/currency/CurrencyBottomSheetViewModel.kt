package xyz.hisname.fireflyiii.ui.currency

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyPagingSource

class CurrencyBottomSheetViewModel(application: Application): BaseViewModel(application) {

    val currencyCode = MutableLiveData<String>()
    val currencyFullDetails = MutableLiveData<String>()

    private val currencyDao = AppDatabase.getInstance(application).currencyDataDao()
    private val currencyService = genericService().create(CurrencyService::class.java)

    fun getCurrencyList() =
            Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
                CurrencyPagingSource(currencyDao, currencyService)
            }.flow.cachedIn(viewModelScope).asLiveData()
}