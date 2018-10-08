package xyz.hisname.fireflyiii.ui.notifications

import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.onboarding.OnboardingActivity
import java.util.*


class NotificationUtils(base: Context) : ContextWrapper(base) {

    private var notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(this)
    private val manager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager


    fun showPiggyBankNotification(contextText: String, contextTitle: String) {
        val PIGGY_BANK_CHANNEL_ID = "xyz.hisname.fireflyiii.PIGGY_BANK"
        val PIGGY_BANK_CHANNEL_NAME = "Piggy Bank"
        val GROUP_ID = "xyz.hisname.fireflyiii.PIGGY_BANK"
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val piggybankChannel = NotificationChannel(PIGGY_BANK_CHANNEL_ID,
                    PIGGY_BANK_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableLights(true)
                description = "Shows Piggy Bank result"
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            manager.createNotificationChannel(piggybankChannel)
        }
        val groupBuilder = NotificationCompat.Builder(this, PIGGY_BANK_CHANNEL_ID).apply {
            setContentText(contextText)
            setGroup(GROUP_ID)
            setSmallIcon(R.drawable.ic_sort_descending)
            setGroupSummary(true)
            setStyle(NotificationCompat.InboxStyle().setBigContentTitle(contextTitle).addLine(contextText))
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        }
        notificationManager.notify(createNotificationId(), groupBuilder.build())

    }

    fun showBillNotification(contextText: String, contextTitle: String){
        val BILL_CHANNEL_ID = "xyz.hisname.fireflyiii.BILL"
        val BILL_CHANNEL_NAME = "Bills"
        val GROUP_ID = "xyz.hisname.fireflyiii.BILL"
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val billChannel = NotificationChannel(BILL_CHANNEL_ID,
                    BILL_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableLights(true)
                description = "Show Bill Notifications"
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            manager.createNotificationChannel(billChannel)
        }
        val groupBuilder = NotificationCompat.Builder(this, BILL_CHANNEL_ID).apply {
            setContentText(contextText)
            setGroup(GROUP_ID)
            setSmallIcon(R.drawable.ic_calendar_blank)
            setGroupSummary(true)
            setStyle(NotificationCompat.InboxStyle().setBigContentTitle(contextTitle).addLine(contextText))
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        }
        notificationManager.notify(createNotificationId(), groupBuilder.build())
    }

    fun showNotSignedIn(){
        val ERROR_CHANNEL_ID = "xyz.hisname.fireflyiii.ERROR"
        val GROUP_ID = "xyz.hisname.fireflyiii.NOT_SIGNED_IN"
        val onboarding = Intent(this, OnboardingActivity::class.java)
        val onboardingIntent = NotificationCompat.Action(R.drawable.app_icon,
                "Click here to sign in",
                PendingIntent.getActivity(this, 0, onboarding, PendingIntent.FLAG_CANCEL_CURRENT))

        val notificationBuilder = NotificationCompat.Builder(this, ERROR_CHANNEL_ID).apply {
            setContentTitle("Error communicating with Firefly")
            setContentText("It appears you are not signed in. Please sign in before continuing.")
            setGroup(GROUP_ID)
            addAction(onboardingIntent)
            setSmallIcon(R.drawable.ic_perm_identity_black_24dp)
            setGroupSummary(true)
        }
        notificationManager.notify(createNotificationId(), notificationBuilder.build())
    }


    private fun createNotificationId(): Int{
        val random = Random()
        var i = random.nextInt(99 + 1)
        i += 1
        return i
    }
}