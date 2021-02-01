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

package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import xyz.hisname.fireflyiii.util.extension.getCompatColor

class BaseSwipeRefreshLayout@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null): SwipeRefreshLayout(context,attrs) {

    init {
        setColorSchemeResources()
    }

    override fun setColorSchemeResources(@ColorRes vararg colorResIds: Int) {
        val colorScheme = arrayListOf(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
        val colorRes = IntArray(colorScheme.size)
        for (i in colorScheme.indices) {
            colorRes[i] = getCompatColor(colorScheme[i])
        }
        setColorSchemeColors(*colorRes)
    }
}