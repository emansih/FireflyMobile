/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.repository.bills

import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import xyz.hisname.fireflyiii.data.local.dao.AttachmentDataDao
import xyz.hisname.fireflyiii.data.local.dao.BillDataDao
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.models.ApiResponses
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.repository.models.bills.BillSuccessModel
import xyz.hisname.fireflyiii.repository.models.error.ErrorModel
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.net.UnknownHostException

@Suppress("RedundantSuspendModifier")
class BillRepository(private val billDao: BillDataDao,
                     private val billService: BillsService) {

    suspend fun getBillById(billId: Long) = billDao.getBillById(billId)

    suspend fun deleteBillById(billId: Long): Int{
        try {
            val networkResponse = billService.deleteBillById(billId)
            when (networkResponse.code()) {
                204 -> {
                    billDao.deleteBillById(billId)
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
                    billDao.deleteBillById(billId)
                    return HttpConstants.NOT_FOUND
                }
                else -> {
                    return HttpConstants.FAILED
                }
            }
        } catch (exception: UnknownHostException){
            billDao.deleteBillById(billId)
            return HttpConstants.FAILED
        }
    }

    suspend fun addBill(name: String, amountMin: String, amountMax: String, date: String, repeatFreq: String,
                        skip: String, active: String, currencyCode: String,
                        notes: String?): ApiResponses<BillSuccessModel>{
        return try {
            val networkCall = billService.createBill(name, amountMin, amountMax, date,
                    repeatFreq, skip, active, currencyCode, notes)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    suspend fun updateBill(billId: Long, name: String, amountMin: String, amountMax: String, date: String,
                           repeatFreq: String, skip: String,active: String, currencyCode: String,
                           notes: String?): ApiResponses<BillSuccessModel>{
        return try {
            val networkCall = billService.updateBill(billId, name, amountMin, amountMax, date,
                    repeatFreq, skip, active, currencyCode, notes)
            parseResponse(networkCall)
        } catch (exception: Exception){
            ApiResponses(error = exception)
        }
    }

    private suspend fun parseResponse(responseFromServer: Response<BillSuccessModel>): ApiResponses<BillSuccessModel> {
        val responseBody = responseFromServer.body()
        val responseErrorBody = responseFromServer.errorBody()
        if(responseBody != null && responseFromServer.isSuccessful){
            billDao.insert(responseBody.data)
            return ApiResponses(response = responseBody)
        } else {
            if(responseErrorBody != null){
                // Ignore lint warning. False positive
                // https://github.com/square/retrofit/issues/3255#issuecomment-557734546
                val moshi = Moshi.Builder().build().adapter(ErrorModel::class.java).fromJson(responseErrorBody.source())
                val errorMessage = when {
                    moshi?.errors?.name != null -> moshi.errors.name[0]
                    moshi?.errors?.currency_code != null -> moshi.errors.currency_code[0]
                    moshi?.errors?.amount_min != null -> moshi.errors.amount_min[0]
                    moshi?.errors?.repeat_freq != null -> moshi.errors.repeat_freq[0]
                    moshi?.errors?.date != null -> moshi.errors.date[0]
                    moshi?.errors?.skip != null -> moshi.errors.skip[0]
                    else -> moshi?.message ?:"Error occurred while saving bill"
                }
                return ApiResponses(errorMessage = errorMessage)
            }
            return ApiResponses(errorMessage = "Error occurred while saving Account")
        }
    }

    suspend fun getAttachment(billId: Long, attachmentDao: AttachmentDataDao): List<AttachmentData>{
        try {
            val networkCall = billService.getBillAttachment(billId)
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                attachmentDao.deleteAttachmentFromJournalId(billId)
                responseBody.data.forEach { attachmentData ->
                    attachmentDao.insert(attachmentData)
                }
            }
        } catch (exception: Exception) { }
        return attachmentDao.getAttachmentFromJournalId(billId)
    }

    suspend fun getAllBills(): Flow<List<String>> {
        try {
            val networkCall = billService.getPaginatedBills(1)
            val billList = arrayListOf<BillData>()
            val responseBody = networkCall.body()
            if(responseBody != null && networkCall.isSuccessful){
                billList.addAll(responseBody.data)
                billDao.deleteAllBills()
                if (responseBody.meta.pagination.current_page != responseBody.meta.pagination.total_pages) {
                    for(pagination in 2..responseBody.meta.pagination.total_pages){
                        val networkBody = billService.getPaginatedBills(pagination).body()
                        if(networkBody != null){
                            billList.addAll(networkBody.data)
                        }
                    }
                }
            }
            billList.forEach { data ->
                billDao.insert(data)
            }
        } catch (exception: Exception){  }
        return billDao.getAllBillName()
    }
}