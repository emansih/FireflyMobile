package xyz.hisname.fireflyiii.util

import android.content.res.Resources

class DeviceUtil{

    companion object {
        fun dpToPx(dp: Int): Int{
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

}