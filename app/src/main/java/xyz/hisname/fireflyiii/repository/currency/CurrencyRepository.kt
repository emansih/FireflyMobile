package xyz.hisname.fireflyiii.repository.currency

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.util.network.HttpConstants

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

    suspend fun updateDefaultCurrency(currency: CurrencyData) = currencyDao.updateDefaultCurrency(currency)

    suspend fun defaultCurrency(): MutableList<CurrencyData> {
        try {
            val networkCall = currencyService?.getDefaultCurrency()
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                val currencyDefault = networkCall.body()?.data?.currencyAttributes?.currencyDefault
                if(currencyDefault == true){
                    // Non buggy version of Firefly III >= 5.3.0
                    deleteDefaultCurrency()
                    insertCurrency(responseBody.data)
                } else {
                    /* _HACK_: Issue #115 ,#112 and #107
                     * Since Firefly III returns a bad response(default = false) between version 5.2.0 to 5.2.8, we store
                     * the response in memory. After that, we will delete the default currency in the database
                     * and insert our bad response. Now, we will update our non-default currency to be the default
                     */
                    deleteDefaultCurrency()
                    insertCurrency(responseBody.data)
                    currencyDao.changeDefaultCurrency(responseBody.data.currencyAttributes?.name ?: "")
                }

            }
        } catch (exception: Exception){ }
        return currencyDao.getDefaultCurrency()
    }

    private suspend fun deleteAllCurrency() = currencyDao.deleteAllCurrency()

    suspend fun getPaginatedCurrency(pageNumber: Int): Flow<MutableList<CurrencyData>> {
        loadPaginatedData(pageNumber)
        return currencyDao.getPaginatedCurrency(pageNumber * Constants.PAGE_SIZE)
    }

    suspend fun getCurrencyCode(currencyName: String) = currencyDao.getCurrencyByName(currencyName)

    suspend fun deleteCurrencyByName(currencyName: String): Int {
        val currencyCode = currencyDao.getCurrencyByName(currencyName)[0].currencyAttributes?.code ?: ""
        try {
            val networkResponse = currencyService?.deleteCurrencyByCode(currencyCode)
            when (networkResponse?.code()) {
                204 -> {
                    currencyDao.deleteCurrencyByCode(currencyCode)
                    return HttpConstants.NO_CONTENT_SUCCESS
                }
                401 -> {
                    /*   User is unauthenticated. We will retain user's data as we are
                     *   now in inconsistent state. This use case is unlikely to happen unless user
                     *   deletes their token from the web interface without updating the mobile client
                     */
                    return HttpConstants.UNAUTHORISED
                }
                404 -> {
                    // User probably deleted this on the web interface and tried to do it using mobile client
                    currencyDao.deleteCurrencyByCode(currencyCode)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception){
            currencyDao.deleteCurrencyByCode(currencyCode)
            return HttpConstants.FAILED
        }
    }

    private suspend fun loadPaginatedData(pageNumber: Int){
        try {
            val networkCall = currencyService?.getSuspendedPaginatedCurrency(pageNumber)
            val responseBody = networkCall?.body()
            if (responseBody != null && networkCall.isSuccessful) {
                if (pageNumber == 1) {
                    deleteAllCurrency()
                }
                responseBody.data.forEachIndexed { _, data ->
                    currencyDao.insert(data)

                }
            }
        } catch (exception: Exception){ }
    }
}