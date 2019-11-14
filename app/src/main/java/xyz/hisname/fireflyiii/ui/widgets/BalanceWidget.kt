package xyz.hisname.fireflyiii.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.SimpleData

class BalanceWidget: AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEachIndexed { index, i ->
            updateWidget(context, appWidgetManager, i)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
                                           appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    private fun simpleData(context: Context): SimpleData{
        return SimpleData(PreferenceManager.getDefaultSharedPreferences(context))
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.balance_homescreen_widget)
        remoteViews.setTextViewText(R.id.widgetText, context.getString(R.string.balance))
        remoteViews.setTextViewText(R.id.widgetAmount, simpleData(context).balance)
        remoteViews.setTextViewText(R.id.moreInfoAmount, simpleData(context).earned + " + "
                + simpleData(context).spent)
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int){
        val remoteViews = RemoteViews(context.packageName, R.layout.balance_homescreen_widget)
        remoteViews.setTextViewText(R.id.widgetAmount, simpleData(context).balance)
        remoteViews.setTextViewText(R.id.moreInfoAmount, simpleData(context).earned + " + "
                + simpleData(context).spent)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}
