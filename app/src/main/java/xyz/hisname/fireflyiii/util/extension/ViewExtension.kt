package xyz.hisname.fireflyiii.util.extension

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
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

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun <T> lazyUnsynchronised(initializer: () -> T): Lazy<T> =
        lazy(LazyThreadSafetyMode.NONE, initializer)

fun <ViewT : View> SupportFragment.bindView(@IdRes idRes: Int): Lazy<ViewT> {
    return lazyUnsynchronised {
        requireActivity().findViewById<ViewT>(idRes)
    }
}

fun View.focusOnView() = this.parent.requestChildFocus(this, this)
