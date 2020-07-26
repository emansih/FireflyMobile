package xyz.hisname.fireflyiii.repository.currency

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

@Suppress("RedundantSuspendModifier")
@WorkerThread
class CurrencyRepository(private val currencyDao: CurrencyDataDao,
                         private val currencyService: CurrencyService?) {

    suspend fun insertCurrency(currency: CurrencyData){
        currencyDao.insert(currency)
    }

    suspend fun getCurrencyByCode(currencyCode: String) = currencyDao.getCurrencyByCode(currencyCode)

    suspend fun getCurrencyById(currencyId: Long) = currencyDao.getCurrencyById(currencyId)

    suspend fun deleteDefaultCurrency() = currencyDao.deleteDefaultCurrency()

    suspend fun defaultCurrencyWithoutNetwork() = currencyDao.getDefaultCurrency()

    suspend fun defaultCurrencyWithNetwork(){
        val networkCall = currencyService?.getDefaultCurrency()
        val responseBody = networkCall?.body()
        if (responseBody != null && networkCall.isSuccessful) {
            deleteDefaultCurrency()
            insertCurrency(responseBody.data)
        }
    }

    private suspend fun deleteAllCurrency() = currencyDao.deleteAllCurrency()

    suspend fun getPaginatedCurrency(pageNumber: Int): MutableList<CurrencyData>{
        loadPaginatedData(pageNumber)
        return currencyDao.getPaginatedCurrency(pageNumber * Constants.PAGE_SIZE)
    }

    private suspend fun loadPaginatedData(pageNumber: Int){
        val networkCall = currencyService?.getSuspendedPaginatedCurrency(pageNumber)
        val responseBody = networkCall?.body()
        if (responseBody != null && networkCall.isSuccessful) {
                if(pageNumber == 1){
                    deleteAllCurrency()
                }
                responseBody.data.forEachIndexed { _, data ->
                    currencyDao.insert(data)

            }
        }
    }
}