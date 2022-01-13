package xyz.hisname.fireflyiii.ui.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.service.AccountListWidgetService
import xyz.hisname.fireflyiii.util.getUniqueHash
import kotlin.random.Random

class AccountListWidget: AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { i ->
            updateWidget(context, appWidgetManager, i)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.account_list_widget)
        val sharedPref = context.getSharedPreferences(
            context.getUniqueHash() + "-user-preferences", Context.MODE_PRIVATE)
        val appPref = AppPref(sharedPref)
        val intent = Intent(context, AccountListWidgetService::class.java)
        remoteViews.setRemoteAdapter(R.id.accountListView, intent)
        remoteViews.setTextViewText(R.id.accountType, appPref.accountListHomeScreenWidget)
        remoteViews.setOnClickPendingIntent(R.id.rightArrow, rightArrowClick(context))
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int){
        val remoteViews = RemoteViews(context.packageName, R.layout.account_list_widget)
        val sharedPref = context.getSharedPreferences(
            context.getUniqueHash() + "-user-preferences", Context.MODE_PRIVATE)
        val appPref = AppPref(sharedPref)
        val intent = Intent(context, AccountListWidgetService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // Required to use a random integer otherwise list would not refresh
        intent.type = Random.nextInt(1000).toString()
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        remoteViews.setOnClickPendingIntent(R.id.rightArrow, rightArrowClick(context))
        val accountListHomeScreenWidget = appPref.accountListHomeScreenWidget
        when {
            accountListHomeScreenWidget.contentEquals("asset") -> {
                remoteViews.setTextViewText(R.id.accountType, context.getText(R.string.asset_account))
            }
            accountListHomeScreenWidget.contentEquals("revenue") -> {
                remoteViews.setTextViewText(R.id.accountType, context.getText(R.string.revenue_account))
            }
            accountListHomeScreenWidget.contentEquals("expense") -> {
                remoteViews.setTextViewText(R.id.accountType, context.getText(R.string.expense_account))
            }
            accountListHomeScreenWidget.contentEquals("liabilities") -> {
                remoteViews.setTextViewText(R.id.accountType, context.getText(R.string.liability_account))
            }
        }
        remoteViews.setRemoteAdapter(R.id.accountListView, intent)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.accountListView)
    }

    private fun rightArrowClick(context: Context): PendingIntent {
        val sharedPref = context.getSharedPreferences(
            context.getUniqueHash() + "-user-preferences", Context.MODE_PRIVATE)
        val appPref = AppPref(sharedPref)
        val accountListHomeScreenWidget = appPref.accountListHomeScreenWidget
        when {
            accountListHomeScreenWidget.contentEquals("asset") -> {
                appPref.accountListHomeScreenWidget = "revenue"
            }
            accountListHomeScreenWidget.contentEquals("revenue") -> {
                appPref.accountListHomeScreenWidget = "expense"
            }
            accountListHomeScreenWidget.contentEquals("expense") -> {
                appPref.accountListHomeScreenWidget = "liabilities"
            }
            accountListHomeScreenWidget.contentEquals("liabilities") -> {
                appPref.accountListHomeScreenWidget = "asset"
            }
        }
        val updateIntent = Intent(context, AccountListWidget::class.java)
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, AccountListWidget::class.java))
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        return PendingIntent.getBroadcast(context, Random.nextInt(2000), updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}