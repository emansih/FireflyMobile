package xyz.hisname.fireflyiii.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.SimpleData

class BillsToPayWidget: AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEachIndexed { index, i ->
            updateWidget(context, appWidgetManager, i)
        }
    }

    private fun simpleData(context: Context): SimpleData {
        return SimpleData(PreferenceManager.getDefaultSharedPreferences(context))
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
                                           appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val views = RemoteViews(context.packageName, R.layout.bills_homescreen_widget)
        views.setTextViewText(R.id.widgetText, context.getString(R.string.bills_to_pay))
        views.setTextViewText(R.id.widgetAmount, simpleData(context).unPaidBills)
        views.setTextViewText(R.id.moreInfoAmount, simpleData(context).paidBills)
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int){
        val views = RemoteViews(context.packageName, R.layout.bills_homescreen_widget)
        views.setTextViewText(R.id.widgetAmount, simpleData(context).unPaidBills)
        views.setTextViewText(R.id.moreInfoAmount, simpleData(context).paidBills)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}