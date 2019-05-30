package xyz.hisname.fireflyiii.util

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import java.text.DecimalFormat

class MpAndroidPercentFormatter: PercentFormatter() {

    private val decimalFormat: DecimalFormat = DecimalFormat("###,###,##0.0")


    override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return decimalFormat.format(value) + "%"
    }
}