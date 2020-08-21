package xyz.hisname.fireflyiii.workers

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.category.CategoryRepository
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.time.Duration
import java.util.concurrent.TimeUnit

class DeleteCategoryWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val categoryDao by lazy { AppDatabase.getInstance(context).categoryDataDao() }

    companion object {
        fun initPeriodicWorker(categoryId: Long, context: Context) {
            val categoryTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("delete_periodic_category_$categoryId").get()
            if (categoryTag == null || categoryTag.size == 0) {
                val categoryData = Data.Builder()
                        .putLong("categoryId", categoryId)
                        .build()
                val delay = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerDelay
                val battery = AppPref(PreferenceManager.getDefaultSharedPreferences(context)).workManagerLowBattery
                val deleteCategoryWork = PeriodicWorkRequestBuilder<DeleteCurrencyWorker>(Duration.ofMinutes(delay))
                        .setInputData(categoryData)
                        .addTag("delete_periodic_category_$categoryId")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(battery)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteCategoryWork)
            }
        }

        fun cancelWorker(categoryId: Long, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("delete_periodic_category_$categoryId")
        }
    }

    override suspend fun doWork(): Result {
        val categoryId = inputData.getLong("categoryId", 0)
        val repository = CategoryRepository(categoryDao, genericService?.create(CategoryService::class.java))
        return when (repository.deleteCategoryById(categoryId)) {
            HttpConstants.NO_CONTENT_SUCCESS -> {
                cancelWorker(categoryId, context)
                Result.success()
            }
            HttpConstants.FAILED -> {
                Result.retry()
            }
            else -> {
                Result.failure()
            }
        }
    }
}