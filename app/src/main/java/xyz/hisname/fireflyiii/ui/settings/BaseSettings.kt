package xyz.hisname.fireflyiii.ui.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat

abstract class BaseSettings: PreferenceFragmentCompat() {

    protected val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun setDivider(divider: Drawable) {
        super.setDivider(ColorDrawable(Color.GRAY))
    }
}