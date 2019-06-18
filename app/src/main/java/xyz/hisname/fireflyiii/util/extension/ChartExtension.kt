package xyz.hisname.fireflyiii.util.extension

import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref

fun PieChart.setData(data: PieChart.() -> Unit){
    if(AppPref(PreferenceManager.getDefaultSharedPreferences(this.context)).nightModeEnabled){
        legend.textColor = getCompatColor(R.color.white)
        description.textColor = getCompatColor(R.color.white)

    }
    description.textSize = 15f
    legend.form = Legend.LegendForm.CIRCLE
    isDrawHoleEnabled = false
    setUsePercentValues(true)
    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
    setTransparentCircleAlpha(0)
    setNoDataText(resources.getString(R.string.no_data_to_generate_chart))
    this.data()
    invalidate()
}