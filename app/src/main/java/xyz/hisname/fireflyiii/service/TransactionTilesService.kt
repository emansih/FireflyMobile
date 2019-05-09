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