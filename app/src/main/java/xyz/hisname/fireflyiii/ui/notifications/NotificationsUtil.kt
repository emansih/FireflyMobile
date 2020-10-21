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


fun Context.displayNotification(contextText: String, contextTitle: String, channelId: String, icon: Int) {
    NotificationUtils(this).showNotification(contextText, contextTitle, channelId, icon)
}

class NotificationUtils(base: Context) : ContextWrapper(base) {

    private val notificationManager by lazy { NotificationManagerCompat.from(this) }
    private inline val Context.manager: NotificationManager get() =
    getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    fun setupChannels(){
        manager.createNotificationChannels(arrayListOf(
                NotificationChannel(Constants.ACCOUNT_CHANNEL,
                        "Accounts", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    description = Constants.ACCOUNT_CHANNEL_DESCRIPTION
                    enableVibration(false)
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                },
                NotificationChannel(Constants.TRANSACTION_CHANNEL,
                        "Transactions", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    description = Constants.TRANSACTION_CHANNEL_DESCRIPTION
                    enableVibration(false)
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                },
                NotificationChannel(Constants.BILL_CHANNEL,
                        "Bill", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    description = Constants.BILL_CHANNEL_DESCRIPTION
                    enableVibration(false)
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                },
                NotificationChannel(Constants.PIGGY_BANK_CHANNEL,
                        "Piggy Bank", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    description = Constants.PIGGY_BANK_CHANNEL_DESCRIPTION
                    enableVibration(false)
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                }
        ))
    }

    fun showNotification(contextText: String, contextTitle: String, channelId: String, icon: Int){
        val groupBuilder = NotificationCompat.Builder(this, channelId).apply {
            setContentText(contextText)
            setGroup(channelId)
            setSmallIcon(icon)
            setGroupSummary(true)
            setStyle(NotificationCompat.BigTextStyle().bigText(contextTitle))
            setStyle(NotificationCompat.BigTextStyle().bigText(contextText))
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        }.build()
        notificationManager.notify(createNotificationId(), groupBuilder)
    }

    fun showNotSignedIn(){
        setUpOreo(Constants.GENERAL_NOTIFICATION, "General Notifications", "Show General Notifications")
        val onboarding = Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val onboardingIntent = NotificationCompat.Action(R.drawable.app_icon,
                "Click here to sign in",
                PendingIntent.getActivity(this, 0, onboarding, PendingIntent.FLAG_CANCEL_CURRENT))
        val notificationBuilder = NotificationCompat.Builder(this, Constants.GENERAL_NOTIFICATION).apply {
            setContentTitle("Error communicating with Firefly")
            setContentText("It appears you are not signed in. Please sign in before continuing.")
            setGroup(Constants.GENERAL_NOTIFICATION)
            addAction(onboardingIntent)
            setSmallIcon(R.drawable.ic_perm_identity_black_24dp)
            setGroupSummary(true)
        }.build()
        notificationManager.notify(createNotificationId(), notificationBuilder)
    }

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