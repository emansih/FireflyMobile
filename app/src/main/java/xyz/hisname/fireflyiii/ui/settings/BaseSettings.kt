package xyz.hisname.fireflyiii.ui.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.preference.PreferenceFragmentCompat
import xyz.hisname.fireflyiii.repository.GlobalViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel

abstract class BaseSettings: PreferenceFragmentCompat() {

    protected val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    protected val globalViewModel by lazy { getViewModel(GlobalViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        handleBackPress()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setDivider(divider: Drawable) {
        super.setDivider(ColorDrawable(Color.GRAY))
    }
    private fun handleBackPress() {
        globalViewModel.backPress.observe(viewLifecycleOwner) { backPressValue ->
            if(backPressValue == true) {
                handleBack()
            }
        }
    }

    abstract fun handleBack()
}