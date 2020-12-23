package xyz.hisname.fireflyiii.workers.category

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.api.CategoryService
import xyz.hisname.fireflyiii.repository.category.CategoryRepository
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.workers.BaseWorker
import java.time.Duration

class CategoryWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters) {

    private val categoryDao by lazy { AppDatabase.getInstance(context).categoryDataDao() }


    companion object {
        fun initPeriodicWorker(categoryName: String, context: Context) {
            val categoryTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("periodic_category_$categoryName").get()
            if (categoryTag == null || categoryTag.size == 0) {
                val categoryData = Data.Builder()
                        .putString("categoryName", categoryName)
                        .build()
                val appPref = AppPref(PreferenceManager.getDefaultSharedPreferences(context))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val deleteCategoryWork = PeriodicWorkRequestBuilder<CategoryWorker>(Duration.ofMinutes(delay))
                        .setInputData(categoryData)
                        .addTag("periodic_category_$categoryName")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteCategoryWork)
            }
        }

        fun cancelWorker(categoryName: String, context: Context){
            WorkManager.getInstance(context).cancelAllWorkByTag("periodic_category_$categoryName")
        }
    }


    override suspend fun doWork(): Result {
        val categoryName = inputData.getString("categoryName")
        val repository = CategoryRepository(categoryDao, genericService.create(CategoryService::class.java))
        val addCategory =  repository.addCategory(categoryName ?: "")
        when {
            addCategory.response != null -> {
                context.showNotification("Category Added", context.getString(R.string.category_added, categoryName), R.drawable.app_icon)
                return Result.success()
            }
            addCategory.errorMessage != null -> {
                context.showNotification("Error Adding $categoryName", addCategory.errorMessage, R.drawable.app_icon)
                return Result.failure()
            }
            addCategory.error != null -> {
                return Result.retry()
            }
            else -> {
                context.showNotification("Error Adding $categoryName", "Please try again later", R.drawable.app_icon)
                cancelWorker(categoryName ?: "", context)
                return Result.failure()
            }
        }
    }
}