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

package xyz.hisname.fireflyiii.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionActivity

@RequiresApi(Build.VERSION_CODES.N)
class TransactionTilesService: TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        tile.state = Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val transactionActivity = Intent(applicationContext, AddTransactionActivity::class.java)
        transactionActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivityAndCollapse(transactionActivity)
    }

}