package xyz.hisname.fireflyiii.util.extension

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment as SupportFragment

fun ViewGroup.inflate(layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun LayoutInflater.create(layoutRes: Int, container: ViewGroup?, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes,container,attachToRoot)
}

inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

fun SupportFragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}

fun Activity.hideKeyboard() {
    hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
}

fun Context.hideKeyboard(view: View?) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun <T> lazyUnsynchronised(initializer: () -> T): Lazy<T> =
        lazy(LazyThreadSafetyMode.NONE, initializer)

fun <ViewT : View> SupportFragment.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnsynchronised {
        requireActivity().findViewById<ViewT>(idRes)
    }
}

fun View.focusOnView() = this.parent.requestChildFocus(this, this)

fun Context.getCompatColor(@ColorRes colorName: Int): Int{
    return ContextCompat.getColor(this, colorName)
}

fun SupportFragment.getCompatColor(@ColorRes colorName: Int): Int{
    return requireActivity().getCompatColor(colorName)
}

fun View.getCompatColor(@ColorRes colorName: Int): Int{
    return context.getCompatColor(colorName)
}

fun Context.getCompatDrawable(@DrawableRes drawableName: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableName)
}

fun SupportFragment.getCompatDrawable(@DrawableRes drawableName: Int): Drawable? {
    return requireActivity().getCompatDrawable(drawableName)
}

fun View.animateBackgroundStateChange(@ColorInt beforeColor: Int? = null, @ColorInt afterColor: Int): ValueAnimator {
    val before = beforeColor ?: (background as? ColorDrawable)?.color ?: Color.TRANSPARENT
    return ValueAnimator.ofObject(ArgbEvaluator(), before, afterColor).apply {
        duration = 250
        addUpdateListener {
            this@animateBackgroundStateChange.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        start()
    }
}