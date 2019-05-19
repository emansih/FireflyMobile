package xyz.hisname.fireflyiii.util

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

class MpAndroidPercentFormatter: ValueFormatter() {

    override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return String.format("%.0f", value) + "%"
    }
}