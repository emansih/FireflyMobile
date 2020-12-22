package xyz.hisname.fireflyiii.ui.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import xyz.hisname.fireflyiii.repository.GlobalViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel

abstract class BaseSettings: PreferenceFragmentCompat() {

    protected val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    protected val globalViewModel by lazy { getViewModel(GlobalViewModel::class.java) }

    override fun setDivider(divider: Drawable) {
        super.setDivider(ColorDrawable(Color.GRAY))
    }
}