package xyz.hisname.fireflyiii.util.extension

import android.app.Activity
import androidx.preference.PreferenceManager
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.StringRes
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import me.toptas.fancyshowcase.listener.DismissListener
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import androidx.fragment.app.Fragment as SupportFragment


fun SupportFragment.showCase(@StringRes title: Int, showOnce: String, layout: View,
                             fitWindow: Boolean = true,
                             dismissListener: DismissListener? = null) =
        requireActivity().showCase(title, showOnce, layout, fitWindow, dismissListener =  dismissListener)


fun Activity.showCase(@StringRes title: Int, showOnce: String, layout: View,  fitWindow: Boolean,
                      dismissListener: DismissListener? = null): FancyShowCaseView{
    val enterAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_from_left)
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
    val showCaseView = FancyShowCaseView.Builder(this)
            .focusOn(layout)
            .title(resources.getString(title))
            .enableAutoTextPosition()
            .showOnce(showOnce)
            .fitSystemWindows(fitWindow)
            .focusShape(FocusShape.ROUNDED_RECTANGLE)
            .enterAnimation(enterAnimation)
            .closeOnTouch(true)
    if(dismissListener != null){
        showCaseView.dismissListener(dismissListener)
    }
    if(AppPref(sharedPref).nightModeEnabled){
        showCaseView.focusBorderColor(R.color.md_green_400)
    }
    return showCaseView.build()
}