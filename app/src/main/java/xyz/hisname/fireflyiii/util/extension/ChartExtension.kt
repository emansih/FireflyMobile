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

package xyz.hisname.fireflyiii.util.extension

import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref

fun PieChart.setData(data: PieChart.() -> Unit){
    if(AppPref(PreferenceManager.getDefaultSharedPreferences(this.context)).nightModeEnabled){
        legend.textColor = getCompatColor(R.color.md_white_1000)
        description.textColor = getCompatColor(R.color.md_white_1000)

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