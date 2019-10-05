package xyz.hisname.fireflyiii.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RemoteViews
import kotlinx.android.synthetic.main.activity_configure_widget.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseActivity

class ConfigurableWidgetActivity: BaseActivity() {

    private val widgetListing by lazy { arrayListOf("Balance Widget", "Left To Spend Widget",
            "Bills To Pay Widget", "Net Worth Widget") }
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_widget)
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        val adapter = ArrayAdapter<String>(this, R.layout.textview_layout, widgetListing)
        widgetList.adapter = adapter
        val widgetManager = AppWidgetManager.getInstance(this)
        widgetList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if(position == 0){
                val views = RemoteViews(packageName, R.layout.balance_widget)
                widgetManager.updateAppWidget(appWidgetId, views)
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            } else if(position == 1){

            } else if(position == 2){

            } else if(position == 3){

            }
        }
    }
}