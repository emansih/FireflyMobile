package xyz.hisname.fireflyiii.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.view.WindowManager

class DeviceUtil{

    companion object {
        fun dpToPx(dp: Int): Int{
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun getScreenHeight(context: Context): Int{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size.y
        }
    }
}