package xyz.hisname.fireflyiii.repository.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.google.gson.Gson
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.transaction.ErrorModel
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.retrofitCallback

class TranscationWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {


    override fun doWork(): Result {
        val notif = NotificationUtils(context)
        val transactionType = inputData.getString("transactionType") ?: ""
        val transactionDescription = inputData.getString("description") ?: ""
        val transactionDate = inputData.getString("date") ?: ""
        val transactionAmount = inputData.getString("amount") ?: ""
        val transactionCurrency = inputData.getString("currency") ?: ""
        val destinationName = inputData.getString("destinationName") ?: ""
        val sourceName = inputData.getString("sourceName") ?: ""
        val piggyBank = inputData.getString("piggyBankName")
        val billName = inputData.getString("billName")
        val category = inputData.getString("categoryName")
        val tags = inputData.getString("tags")
        val budget = inputData.getString("budgetName")
        val interestDate  = inputData.getString("interestDate")
        val bookDate = inputData.getString("bookDate")
        val processDate = inputData.getString("processDate")
        val dueDate = inputData.getString("dueDate")
        val paymentDate = inputData.getString("paymentDate")
        val invoiceDate = inputData.getString("invoiceDate")
        val transactionService = RetrofitBuilder.getClient(baseUrl, accessToken)?.
                create(TransactionService::class.java)
        transactionService?.addTransaction(convertString(transactionType), transactionDescription, transactionDate, piggyBank,
                billName, transactionAmount,sourceName, destinationName, transactionCurrency, category,
                tags, budget, interestDate, bookDate, processDate, dueDate, paymentDate,
                invoiceDate)?.enqueue(
                retrofitCallback({ response ->
                    var errorBody = ""
                    if (response.errorBody() != null) {
                        errorBody = String(response.errorBody()?.bytes()!!)
                    }
                    val gson = Gson().fromJson(errorBody, ErrorModel::class.java)
                    if(response.isSuccessful){
                        notif.showTransactionNotification("Transaction added successfully!", "Transaction")
                        Result.SUCCESS
                    } else {
                        var error = ""
                        when {
                            gson.errors.transactions_destination_name != null -> {
                                error = gson.errors.transactions_destination_name[0]
                            }
                            gson.errors.transactions_currency != null -> {
                                error = gson.errors.transactions_currency[0]
                            }
                            gson.errors.transactions_source_name != null  -> {
                                error = gson.errors.transactions_source_name[0]
                            }
                        }
                        notif.showTransactionNotification(error, "Error Adding $transactionType")
                        Result.FAILURE
                    }
                })
                { throwable ->
                    notif.showTransactionNotification(throwable.message.toString(), "Error adding Transaction")
                    Result.FAILURE
                })
        return Result.SUCCESS
    }

    private fun convertString(type: String) = type.substring(0,1).toLowerCase() + type.substring(1).toLowerCase()

}