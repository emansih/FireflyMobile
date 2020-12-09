package xyz.hisname.fireflyiii.repository.currency

import androidx.annotation.WorkerThread
import com.squareup.moshi.Moshi
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencySuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.HttpConstants

@Suppress("RedundantSuspendModifier")
@WorkerThread
class CurrencyRepository(private val currencyDao: CurrencyDataDao,
                         private val currencyService: CurrencyService) {

    suspend fun insertCurrency(currency: CurrencyData){
        currencyDao.insert(currency)
    }

    suspend fun getCurrencyFromBillId(billId: Long, currencyCode: String): String {
        try {
            val currencyList = currencyDao.getCurrencyFromBill(billId)
            if(currencyList.isEmpty()){
                val networkCall = currencyService.getCurrencyByCode(currencyCode)
                val responseBody = networkCall.body()
                if (responseBody != null && networkCall.isSuccessful) {
                    currencyDao.deleteCurrencyByCode(currencyCode)
                    responseBody.data.forEach { currencyData ->
                        currencyDao.insert(currencyData)
                    }
                }
            }
        } catch (exception: Exception){ }
        return currencyDao.getCurrencyFromBill(billId)
    }

    suspend fun getCurrencyByCode(currencyCode: String): List<CurrencyData> {
        try {
            val networkCall = currencyService.getCurrencyByCode(currencyCode)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                currencyDao.deleteCurrencyByCode(currencyCode)
                responseBody.data.forEach { currencyData ->
                    currencyDao.insert(currencyData)
                }
            }
        } catch (exception: Exception){ }
        return currencyDao.getCurrencyByCode(currencyCode)
    }

    suspend fun getCurrencyById(currencyId: Long) = currencyDao.getCurrencyById(currencyId)

    suspend fun deleteDefaultCurrency() = currencyDao.deleteDefaultCurrency()

    suspend fun defaultCurrency(): CurrencyData {
        try {
            val networkCall = currencyService.getDefaultCurrency()
            val responseBody = networkCall.body()
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

    suspend fun getCurrencyCode(currencyName: String) = currencyDao.getCurrencyByName(currencyName)

    suspend fun addCurrency(name: String, code: String, symbol: String, decimalPlaces: String,
                           enabled: Boolean,default: Boolean): ApiResponses<CurrencySuccessModel>{
        return try {
            val networkCall = currencyService.addCurrency(name, code, symbol, decimalPlaces, enabled, default)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updateCurrency(name: String, code: String, symbol: String, decimalPlaces: String,
                               enabled: Boolean,default: Boolean): ApiResponses<CurrencySuccessModel>{
        return try {
            val networkCall = currencyService.updateCurrency(code, name, code, symbol, decimalPlaces, enabled, default)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }


    private suspend fun parseResponse(responseFromServer: Response<CurrencySuccessModel>?): ApiResponses<CurrencySuccessModel>{
        val responseBody = responseFromServer?.body()
        val responseErrorBody = responseFromServer?.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                var errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.code != null -> moshi.errors.code[0]
                    moshi?.errors?.symbol != null -> moshi.errors.symbol[0]
                    moshi?.errors?.decimalPlaces != null -> moshi.errors.decimalPlaces[0]
                    else -> "Error occurred while saving currency"
                }
                return ApiResponses(errorMessage = errorMessage)
            } else {
                insertCurrency(responseBody.data)
                return ApiResponses(response = responseBody)
            }
        } else {
            return ApiResponses(errorMessage = "Error occurred while saving currency")
        }
    }

    suspend fun deleteCurrencyByCode(currencyCode: String): Int {
        try {
            val networkResponse = currencyService.deleteCurrencyByCode(currencyCode)
            when (networkResponse.code()) {
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

    // Expensive network call. Use appropriately
    suspend fun getAllCurrency(): List<CurrencyData>{
        val currencyDataList = arrayListOf<CurrencyData>()
        try {
            val networkCall = currencyService.getPaginatedCurrency(1)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                currencyDataList.addAll(responseBody.data)
                if (responseBody.meta.pagination.total_pages != 1) {
                    for (items in 2..responseBody.meta.pagination.total_pages) {
                        val repeatedCall = currencyService.getPaginatedCurrency(items)
                        val repeatedCallResponse = repeatedCall.body()
                        if(repeatedCallResponse != null && networkCall.isSuccessful){
                            currencyDataList.addAll(repeatedCallResponse.data)
                        }
                    }
                }
                currencyDao.deleteAllCurrency()
                currencyDataList.forEach { data ->
                    currencyDao.insert(data)
                }
            }
        } catch (exception: Exception){ }
        return currencyDao.getSortedCurrency()
    }
}