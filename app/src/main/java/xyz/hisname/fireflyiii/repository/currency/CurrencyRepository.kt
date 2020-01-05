package xyz.hisname.fireflyiii.repository.currency

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyModel

@Suppress("RedundantSuspendModifier")
@WorkerThread
class CurrencyRepository(private val currencyDao: CurrencyDataDao,
                         private val currencyService: CurrencyService?) {

    suspend fun insertCurrency(currency: CurrencyData){
        currencyDao.insert(currency)
    }

    suspend fun getCurrencyByCode(currencyCode: String): MutableList<CurrencyData>{
        return currencyDao.getCurrencyByCode(currencyCode)
    }

    suspend fun getCurrencyById(currencyId: Long) = currencyDao.getCurrencyById(currencyId)

    suspend fun deleteDefaultCurrency() = currencyDao.deleteDefaultCurrency()

    suspend fun defaultCurrency() = currencyDao.getDefaultCurrency()

    suspend fun deleteAllCurrency() = currencyDao.deleteAllCurrency()

    suspend fun allCurrency() = currencyDao.getAllCurrency()

    suspend fun getPaginatedCurrency(pageNumber: Int): MutableList<CurrencyData>{
        loadPaginatedData(pageNumber)
        return currencyDao.getPaginatedCurrency(pageNumber * Constants.PAGE_SIZE)
    }

    private suspend fun loadPaginatedData(pageNumber: Int){
        var networkCall: Response<CurrencyModel>? = null
        try {
            withContext(Dispatchers.IO) {
                networkCall = currencyService?.getSuspendedPaginatedCurrency(pageNumber)
            }
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall?.isSuccessful != false) {
                withContext(Dispatchers.IO){
                    if(pageNumber == 1){
                        deleteAllCurrency()
                    }
                    responseBody.data.forEachIndexed { _, data ->
                        currencyDao.insert(data)
                    }
                }
            }
        } catch (exception: Exception){
        }
    }


}