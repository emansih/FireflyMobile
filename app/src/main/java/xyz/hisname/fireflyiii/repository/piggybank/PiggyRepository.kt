package xyz.hisname.fireflyiii.repository.piggybank

import androidx.paging.PagingSource
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.AttachmentDataDao
import xyz.hisname.fireflyiii.data.local.dao.PiggyDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.PiggybankService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggySuccessModel
import xyz.hisname.fireflyiii.util.extension.debounce
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

    suspend fun getPiggyNames(): Flow<List<String>>{
        try {
            val networkCall = piggyService.getPaginatedPiggyBank(1)
            val responseBody = networkCall.body()
            if (responseBody != null && networkCall.isSuccessful) {
                piggyDao.deleteAllPiggyBank()
                responseBody.data.forEach { data ->
                    piggyDao.insert(data)
                }
                val pagination = responseBody.meta.pagination
                if (pagination.total_pages != pagination.current_page) {
                    for (items in 2..pagination.total_pages) {
                        val service = piggyService.getPaginatedPiggyBank(items).body()
                        service?.data?.forEach { dataToBeAdded ->
                            piggyDao.insert(dataToBeAdded)
                        }
                    }
                }
            }
        } catch (exception: Exception){ }
        return piggyDao.getAllPiggyName()
    }


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
            piggyDao.insert(responseBody.data)
            return ApiResponses(response = responseBody)
        } else {
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(responseErrorBody.source())
                val errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.account_id != null -> moshi.errors.account_id[0]
                    moshi?.errors?.current_amount != null -> moshi.errors.current_amount[0]
                    moshi?.errors?.targetDate != null -> moshi.errors.targetDate[0]
                    else -> moshi?.message ?: "Error occurred while saving piggy bank"
                }
                return ApiResponses(errorMessage = errorMessage)
            }
            return ApiResponses(errorMessage = "Error occurred while saving piggy bank")
        }
    }


    suspend fun getAttachment(piggyId: Long, attachmentDao: AttachmentDataDao): List<AttachmentData>{
        try {
            val networkCall = piggyService.getPiggyBankAttachment(piggyId)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                responseBody.data.forEach { attachmentData ->
                    attachmentDao.insert(attachmentData)
                }
            }
        } catch (exception: Exception) { }
        return attachmentDao.getAttachmentFromJournalId(piggyId)
    }
}