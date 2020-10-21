package xyz.hisname.fireflyiii.util.extension

import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.IconCompat
import io.karn.notify.Notify
import io.karn.notify.internal.utils.Action

fun Context.showNotification(notificationTitle: String, notificationText: String,
                             @DrawableRes notificationIcon: Int){
    Notify.with(this)
            .content {
                title = notificationTitle
                text = notificationText
            }
            .header {
                icon = notificationIcon
            }
            .show()
}

fun Context.showNotification(notificationTitle: String, notificationText: String,
                             @DrawableRes notificationIcon: Int, pendingIntent: PendingIntent,
                             actionString: String, actionIcon: IconCompat){
    Notify.with(this)
            .content {
                title = notificationTitle
                text = notificationText
            }
            .header {
                icon = notificationIcon
            }
            .actions {
                add(Action(
                        actionIcon, actionString, pendingIntent
                ))
            }
            .show()
}