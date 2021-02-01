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