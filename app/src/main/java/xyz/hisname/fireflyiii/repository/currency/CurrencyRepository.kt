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
import xyz.hisname.fireflyiii.repository.models.currency.DefaultCurrencyModel
import java.lang.Exception

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
        var networkCall: Response<DefaultCurrencyModel>? = null
        withContext(Dispatchers.IO) {
            try {
                networkCall = currencyService?.getDefaultCurrency()
            } catch (exception: Exception){

            }
        }
        val responseBody = networkCall?.body()
        if (responseBody != null && networkCall?.isSuccessful != false) {
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
        var networkCall: Response<CurrencyModel>?
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
    }


}