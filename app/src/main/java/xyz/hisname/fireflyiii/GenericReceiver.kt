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

package xyz.hisname.fireflyiii

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import xyz.hisname.fireflyiii.ui.HomeActivity

class GenericReceiver: BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("transaction_notif")
        if(action != null) {
            // close the notification tray
            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            val trans = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            when (action) {
                "Deposit" -> trans.putExtra("transaction", "Deposit")
                "Withdrawal" -> trans.putExtra("transaction", "Withdrawal")
                "Transfer" -> trans.putExtra("transaction", "Transfer")
            }
            context.startActivity(trans)
        }
    }
}