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

package xyz.hisname.fireflyiii.ui.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.SimpleData
import xyz.hisname.fireflyiii.util.getUniqueHash

class BillsToPayWidget: AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEachIndexed { index, i ->
            updateWidget(context, appWidgetManager, i)
        }
    }

    private fun simpleData(context: Context): SimpleData {
        return SimpleData(context.getSharedPreferences(context.getUniqueHash().toString() + "-user-preferences",
            Context.MODE_PRIVATE))
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