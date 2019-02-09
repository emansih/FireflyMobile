package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

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
            colorRes[i] = ContextCompat.getColor(context, colorScheme[i])
        }
        setColorSchemeColors(*colorRes)
    }
}