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

package xyz.hisname.fireflyiii.ui.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.GenericReceiver
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.onboarding.AuthActivity
import java.util.*



class NotificationUtils(base: Context) : ContextWrapper(base) {

    private val notificationManager by lazy { NotificationManagerCompat.from(this) }
    private inline val Context.manager: NotificationManager
        get() = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    fun showTransactionPersistentNotification(){
        setUpOreo(Constants.TRANSACTION_CHANNEL, "Transaction", "Shows Persistent Transaction Notifications")
        val expenseIntent = Intent(this, GenericReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("transaction_notif", "Withdrawal")
        }
        val expensePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, expenseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val incomeIntent = Intent(this, GenericReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("transaction_notif", "Deposit")
        }
        val incomePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 1, incomeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val transferIntent = Intent(this, GenericReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("transaction_notif", "Transfer")
        }
        val transferPendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 2, transferIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(this, Constants.TRANSACTION_CHANNEL).apply {
            setContentText("Add transactions anytime anywhere")
            setSmallIcon(R.drawable.ic_refresh)
            setAutoCancel(false)
            setOngoing(true)
            addAction(R.drawable.ic_arrow_left, "Expenses", expensePendingIntent)
            addAction(R.drawable.ic_arrow_right, "Income", incomePendingIntent)
            addAction(R.drawable.ic_bank_transfer, "Transfer", transferPendingIntent)
            setStyle(NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Transactions shortcut")
                    .bigText("Tap here to add transactions!"))
        }.build()
        notificationManager.notify("transaction_notif",12345, notificationBuilder)
    }

    private fun setUpOreo(channelId: String, channelName: String, channelDescription: String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                description = channelDescription
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            manager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotificationId(): Int{
        val random = Random()
        var i = random.nextInt(99 + 1)
        i += 1
        return i
    }
}