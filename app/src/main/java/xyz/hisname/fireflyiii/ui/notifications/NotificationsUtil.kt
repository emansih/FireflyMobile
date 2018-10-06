package xyz.hisname.fireflyiii.ui.notifications

import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import xyz.hisname.fireflyiii.R


class NotificationUtils(base: Context) : ContextWrapper(base) {

    @Volatile private var notificationManager: NotificationManager? = null
    private val PIGGY_BANK_CHANNEL_ID = "xyz.hisname.fireflyiii.PIGGY_BANK"
    private val PIGGY_BANK_CHANNEL_NAME = "Piggy Bank"

    private val manager: NotificationManager?
        get() {
            if (notificationManager == null) {
                synchronized(NotificationUtils::class.java) {
                    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                }
            }
            return notificationManager
        }


    fun showPiggyBankNotification(contextText: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val piggybankChannel = NotificationChannel(PIGGY_BANK_CHANNEL_ID,
                    PIGGY_BANK_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            piggybankChannel.enableLights(true)
            piggybankChannel.description = "Shows Piggy Bank result"
            piggybankChannel.enableVibration(false)
            piggybankChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager?.createNotificationChannel(piggybankChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this,  PIGGY_BANK_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_sort_descending)
                .setContentTitle("Piggy bank")
                .setContentText(contextText)
        notificationManager?.notify(1, notificationBuilder.build())

    }
}