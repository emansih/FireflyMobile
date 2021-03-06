/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.hisname.fireflyiii.repository.models.transaction.SplitSeparator
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
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

fun Flow<PagingData<Transactions>>.insertDateSeparator() =
    this.map { pagingData ->
        pagingData.map { transactions ->
            SplitSeparator.TransactionItem(transactions)
        }.insertSeparators { before, after ->
            if (before == null) {
                if(after != null){
                    return@insertSeparators SplitSeparator.SeparatorItem(after.transaction.date.dayOfMonth.toString()
                            + " " + after.transaction.date.month + " "
                            + after.transaction.date.year)
                }
                return@insertSeparators null
            }

            if(after == null){
                return@insertSeparators null
            }

            if (after.transaction.date.isAfter(before.transaction.date)) {
                SplitSeparator.SeparatorItem(after.transaction.date.dayOfMonth.toString()
                        + " " + after.transaction.date.month + " "
                        + after.transaction.date.year)
            } else {
                null
            }
        }

    }
