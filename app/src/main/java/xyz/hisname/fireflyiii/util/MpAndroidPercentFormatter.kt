package xyz.hisname.fireflyiii.util

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

class MpAndroidPercentFormatter: IValueFormatter {

    override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return String.format("%.0f", value) + "%"
    }
}