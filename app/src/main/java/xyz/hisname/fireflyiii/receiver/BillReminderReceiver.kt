package xyz.hisname.fireflyiii.receiver

import android.accounts.AccountManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.NewAccountManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.util.extension.showNotification
import xyz.hisname.fireflyiii.util.network.CustomCa
import java.io.File

class BillReminderReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val billId = intent.getLongExtra("billId", 0)
        val userId = intent.getStringExtra("userId")
        if(billId != 0L && !userId.isNullOrBlank()){
            val billService = genericService(userId, context).create(BillsService::class.java)
            val billDao = AppDatabase.getInstance(context, userId).billDataDao()
            val billRepository = BillRepository(billDao, billService)
            runBlocking(Dispatchers.IO){
                val bill = billRepository.getBillById(billId)
                if(bill.billId != 0L && bill.billAttributes.active){
                    context.showNotification(bill.billAttributes.name + " is due",
                        "Please pay " +
                                bill.billAttributes.currency_symbol + bill.billAttributes.amount_min + " soon",
                        R.drawable.ic_calendar_blank)
                }
            }
        }
    }


    private fun genericService(userId: String, context: Context): Retrofit {
        val cert = AppPref(sharedPref(userId, context)).certValue
        val certFile = File(context.filesDir.path + "/" + userId + ".pem")
        return if (certFile.exists()) {
            val customCa = CustomCa(certFile)
            FireflyClient.getClient(getUrl(userId, context), newManager(userId, context).accessToken,
                cert, customCa.getCustomTrust(), customCa.getCustomSSL())
        } else {
            FireflyClient.getClient(getUrl(userId, context),
                newManager(userId, context).accessToken, cert, null, null)
        }
    }

    private fun sharedPref(userId: String, context: Context): SharedPreferences{
        return context.getSharedPreferences("$userId-user-preferences", Context.MODE_PRIVATE)
    }

    private fun getUrl(userId: String, context: Context): String {
        val activeUrl: String
        runBlocking(Dispatchers.IO){
            activeUrl = FireflyUserDatabase.getInstance(context).fireflyUserDao().getUserByHash(userId).userHost
        }
        return activeUrl
    }

    private fun newManager(userId: String, context: Context): NewAccountManager {
        return NewAccountManager(AccountManager.get(context), userId)
    }
}