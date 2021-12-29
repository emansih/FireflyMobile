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

package xyz.hisname.fireflyiii.workers.category

import android.content.Context
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

    companion object {
        fun initPeriodicWorker(categoryName: String, context: Context, uuid: String) {
            val categoryTag =
                    WorkManager.getInstance(context).getWorkInfosByTag("periodic_category_$categoryName" + "_$uuid").get()
            if (categoryTag == null || categoryTag.size == 0) {
                val categoryData = Data.Builder()
                    .putString("categoryName", categoryName)
                    .putString("uuid", uuid)
                    .build()
                val appPref = AppPref(context.getSharedPreferences("$uuid-user-preferences", Context.MODE_PRIVATE))
                val delay = appPref.workManagerDelay
                val battery = appPref.workManagerLowBattery
                val networkType = appPref.workManagerNetworkType
                val requireCharging = appPref.workManagerRequireCharging
                val deleteCategoryWork = PeriodicWorkRequestBuilder<CategoryWorker>(Duration.ofMinutes(delay))
                        .setInputData(categoryData)
                        .addTag("periodic_category_$categoryName" + "_$uuid")
                        .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(networkType)
                                .setRequiresBatteryNotLow(battery)
                                .setRequiresCharging(requireCharging)
                                .build())
                        .build()
                WorkManager.getInstance(context).enqueue(deleteCategoryWork)
            }
        }

        fun cancelWorker(categoryName: String, context: Context, uuid: String){
            WorkManager.getInstance(context).cancelAllWorkByTag("periodic_category_$categoryName" + "_$uuid")
        }
    }


    override suspend fun doWork(): Result {
        val categoryDao = AppDatabase.getInstance(context, uuid).categoryDataDao()
        val categoryName = inputData.getString("categoryName")
        val repository = CategoryRepository(categoryDao, genericService(uuid).create(CategoryService::class.java))
        val addCategory =  repository.addCategory(categoryName ?: "")
        when {
            addCategory.response != null -> {
                context.showNotification("Category Added", context.getString(R.string.category_added, categoryName), R.drawable.app_icon)
                cancelWorker(categoryName ?: "", context, uuid)
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
                cancelWorker(categoryName ?: "", context, uuid)
                return Result.failure()
            }
        }
    }
}