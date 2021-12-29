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

package xyz.hisname.fireflyiii.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.utils.toAndroidIconCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.workers.transaction.TransactionWorker
import xyz.hisname.fireflyiii.ui.onboarding.AuthActivity
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.util.getUniqueHash
import java.time.OffsetDateTime
import java.util.concurrent.ThreadLocalRandom

// TODO: Remove this "automation" code and probably migrate to tasker. I really hate this piece of code
class TransactionReceiver: BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        val uniqueHash = context.getUniqueHash()
        if(AppPref(context.getSharedPreferences(uniqueHash + "-user-preferences",
                Context.MODE_PRIVATE)).baseUrl.isBlank()){
            val onboarding = Intent(context, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val icon = IconicsDrawable(context).apply {
                icon = GoogleMaterial.Icon.gmd_lock
                sizeDp = 24
            }.toAndroidIconCompat()
           context.showNotification("Unauthenticated",
                   "It appears you are not signed in. Please sign in before continuing",
                   R.drawable.ic_perm_identity_black_24dp,
                   PendingIntent.getActivity(context, 0, onboarding, PendingIntent.FLAG_CANCEL_CURRENT),
           "Sign in", icon)
        } else {
            val transactionWorkManagerId = ThreadLocalRandom.current().nextLong()
            val destinationName = intent.getStringExtra("destination") ?: ""
            val sourceName = intent.getStringExtra("source") ?: ""
            val category = intent.getStringExtra("category")
            val transactionAmount = intent.getStringExtra("amount")
            val budget = intent.getStringExtra("budget")
            val description =  intent.getStringExtra("description") ?: ""
            val tags = intent.getStringExtra("tags")
            val time = intent.getStringExtra("time")
            val date = intent.getStringExtra("date")
            val piggyBank = intent.getStringExtra("piggybank")
            val bill = intent.getStringExtra("bill")
            val notes = intent.getStringExtra("notes")
            val transactionData = Data.Builder()
                .putString("description", description)
                .putString("date", date)
                .putString("time", time)
                .putString("amount", transactionAmount)
                .putString("currency", intent.getStringExtra("currency"))
                .putString("tags", tags)
                .putString("categoryName", category)
                .putString("bill", bill)
                .putString("budgetName", budget)
                .putLong("transactionWorkManagerId", transactionWorkManagerId)
                .putString("sourceName", sourceName)
                .putString("destinationName", destinationName)
                .putString("piggyBankName",piggyBank)
                .putString("notes", notes)
                .putString("uuid", uniqueHash)
            val transactionDatabase = AppDatabase.getInstance(context, uniqueHash).transactionDataDao()
            val currencyDatabase = AppDatabase.getInstance(context, uniqueHash).currencyDataDao()
            var currency: CurrencyData
            runBlocking(Dispatchers.IO) {
                currency = currencyDatabase.getCurrencyByCode(intent.getStringExtra("currency") ?: "")[0]
            }
            val currencyAttributes = currency.currencyAttributes
            val tagsList = arrayListOf<String>()
            if(tags != null){
                tagsList.addAll(tags.split(",").map { it.trim() })
            }
            var transactionType = ""
            when (intent.action) {
                "firefly.hisname.ADD_DEPOSIT" -> {
                    TransactionWorker.initWorker(context, transactionData, "deposit",
                            transactionWorkManagerId, arrayListOf(), uniqueHash)
                    transactionType = "deposit"
                }
                "firefly.hisname.ADD_WITHDRAW" -> {
                    TransactionWorker.initWorker(context, transactionData, "withdrawal",
                            transactionWorkManagerId, arrayListOf(), uniqueHash)
                    transactionType = "withdrawal"
                }
                "firefly.hisname.ADD_TRANSFER" -> {
                    TransactionWorker.initWorker(context, transactionData, "transfer",
                            transactionWorkManagerId, arrayListOf(), uniqueHash)
                    transactionType = "transfer"
                }
                else -> { }
            }
            val dateTime = if(time.isNullOrEmpty()){
                DateTimeUtil.offsetDateTimeWithoutTime(date)
            } else {
                DateTimeUtil.mergeDateTimeToIso8601(date ?: "", time)
            }
            val amount = if(transactionAmount.isNullOrEmpty()){
                0.0
            } else {
                transactionAmount.toDouble()
            }
            runBlocking(Dispatchers.IO){
                transactionDatabase.insert(
                        Transactions(
                                transactionWorkManagerId, amount,  0,
                                 budget, 0,  category, currencyAttributes.code,
                                currencyAttributes.decimal_places, currency.currencyId,
                                currencyAttributes.name, currencyAttributes.symbol,
                                OffsetDateTime.parse(dateTime), description, 0, destinationName,
                                "", 0, bill, "",  0.0, "","", 0,
                                "", notes, 0, "", 0,
                                sourceName, "", "",
                                tagsList, transactionType, 0, piggyBank,true, arrayListOf())
                )
            }
        }
    }
}