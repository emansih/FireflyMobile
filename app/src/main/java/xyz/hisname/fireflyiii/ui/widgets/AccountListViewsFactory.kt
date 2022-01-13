package xyz.hisname.fireflyiii.ui.widgets

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import timber.log.Timber
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

class AccountListViewsFactory(private val accountDataList: ArrayList<AccountData>,
                              private val context: Context): RemoteViewsService.RemoteViewsFactory {


    override fun onCreate() {

    }

    override fun onDataSetChanged() {
    }

    override fun onDestroy() {
    }

    override fun getCount() = accountDataList.size

    override fun getViewAt(position: Int): RemoteViews {
        val accountAttributes = accountDataList[position].accountAttributes
        val remoteView = RemoteViews(context.packageName, R.layout.account_list_item_widget)
        remoteView.setTextViewText(R.id.accountNameText, accountAttributes.name)
        remoteView.setTextViewText(R.id.accountAmountText,
            accountAttributes.currency_symbol +  accountAttributes.current_balance)
        return remoteView
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true
}