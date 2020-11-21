package xyz.hisname.fireflyiii.util.extension

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    ViewCompat.getWindowInsetsController(window.decorView.findViewById(android.R.id.content))?.hide(WindowInsetsCompat.Type.ime())
}


fun <T> lazyUnsynchronised(initializer: () -> T): Lazy<T> =
        lazy(LazyThreadSafetyMode.NONE, initializer)

fun <ViewT : View> SupportFragment.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnsynchronised {
        requireActivity().findViewById(idRes)
    }
}

fun <ViewT : View> AppCompatActivity.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnsynchronised {
        findViewById(idRes)
    }
}

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

fun View.isOverlapping(secondView: View): Boolean {
    val firstPosition = IntArray(2)
    val secondPosition = IntArray(2)
    getLocationOnScreen(firstPosition)
    secondView.getLocationOnScreen(secondPosition)
    val rectFirstView = Rect(firstPosition[0], firstPosition[1], firstPosition[0] + measuredWidth, firstPosition[1] + measuredHeight)
    val rectSecondView = Rect(secondPosition[0], secondPosition[1], secondPosition[0] + secondView.measuredWidth, secondPosition[1] + secondView.measuredHeight)
    return rectFirstView.intersect(rectSecondView)
}