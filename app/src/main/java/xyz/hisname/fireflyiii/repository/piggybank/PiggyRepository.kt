package xyz.hisname.fireflyiii.repository.piggybank

import com.squareup.moshi.Moshi
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggySuccessModel
import xyz.hisname.fireflyiii.util.network.HttpConstants

@Suppress("RedundantSuspendModifier")
class PiggyRepository(private val piggyDao: PiggyDataDao,
                      private val piggyService: PiggybankService) {

    suspend fun deletePiggyById(piggyId: Long): Int {
        try {
            val networkResponse = piggyService.deletePiggyBankById(piggyId)
            when (networkResponse.code()) {
                204 -> {
                    piggyDao.deletePiggyById(piggyId)
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
                    piggyDao.deletePiggyById(piggyId)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: Exception){
            // User is offline.
            piggyDao.deletePiggyById(piggyId)
            return HttpConstants.FAILED
        }
    }

    suspend fun getPiggyById(piggyId: Long) =  piggyDao.getPiggyFromId(piggyId)

    // Since there is no API to search piggy bank, we simply do a query in the piggy bank
    suspend fun searchPiggyBank(searchQuery: String) = piggyDao.searchPiggyName("*$searchQuery*")

    suspend fun addPiggyBank(name: String, accountId: Long, targetAmount: String,
                             currentAmount: String?, startDate: String?, endDate: String?, notes: String?): ApiResponses<PiggySuccessModel> {
        return try {
            val networkCall = piggyService.addPiggyBank(name, accountId, targetAmount, currentAmount, startDate, endDate, notes)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updatePiggyBank(piggyId: Long, name: String, accountId: Long, targetAmount: String,
                                currentAmount: String?, startDate: String?, endDate: String?, notes: String?): ApiResponses<PiggySuccessModel>{
        return try {
            val networkCall = piggyService.updatePiggyBank(piggyId, name, accountId, targetAmount, currentAmount, startDate, endDate, notes)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    private suspend fun parseResponse(responseFromServer: Response<PiggySuccessModel>): ApiResponses<PiggySuccessModel>{
        val responseBody = responseFromServer.body()
        val responseErrorBody = responseFromServer.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                var errorMessage = String(responseErrorBody.bytes())
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(errorMessage)
                errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.account_id != null -> moshi.errors.account_id[0]
                    moshi?.errors?.current_amount != null -> moshi.errors.current_amount[0]
                    moshi?.errors?.targetDate != null -> moshi.errors.targetDate[0]
                    else -> "Error occurred while saving piggy bank"
                }
                return ApiResponses(errorMessage = errorMessage)
            } else {
                piggyDao.insert(responseBody.data)
                return ApiResponses(response = responseBody)
            }
        } else {
            return ApiResponses(errorMessage = "Error occurred while saving piggy bank")
        }
    }

}