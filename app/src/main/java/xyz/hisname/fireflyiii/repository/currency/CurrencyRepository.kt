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

    // Issue: https://github.com/firefly-iii/firefly-iii/issues/3493
    @Deprecated("Only use this if user's Firefly III version is less than or equal to 5.2.8")
    suspend fun loadAllData(){
        var networkCall: Response<CurrencyModel>? = null
        val defaultCurrencyList: MutableList<CurrencyData> = arrayListOf()
        withContext(Dispatchers.IO) {
            try {
                networkCall = currencyService?.getSuspendedPaginatedCurrency(1)
                val responseBody = networkCall?.body()
                if (responseBody != null && networkCall?.isSuccessful != false) {
                    withContext(Dispatchers.IO){
                        defaultCurrencyList.addAll(responseBody.data)
                        if(responseBody.meta.pagination.total_pages > 1){
                            for(pagination in 2..responseBody.meta.pagination.total_pages){
                                val currencyCall = currencyService?.getSuspendedPaginatedCurrency(pagination)
                                currencyCall?.body()?.data?.let { defaultCurrencyList.addAll(it) }
                            }
                        }
                        deleteDefaultCurrency()
                        defaultCurrencyList.forEach {
                            currencyDao.insert(it)

                        }
                    }
                }
            } catch (exception: Exception){

            }
        }

    }

    private suspend fun loadPaginatedData(pageNumber: Int){
        var networkCall: Response<CurrencyModel>? = null
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