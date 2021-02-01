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

package xyz.hisname.fireflyiii.util.animation

import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

class CircularReveal(private val revealView: View) {

    fun showReveal(revealX: Int, revealY: Int, animation: BakedBezierInterpolator = BakedBezierInterpolator.FADE_IN_CURVE) {
        revealView.isInvisible = true
        val viewTreeObserver = revealView.viewTreeObserver
        if(viewTreeObserver.isAlive){
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
                override fun onGlobalLayout() {
                    val finalRadius = (Math.max(revealView.width,
                            revealView.height) * 1.1).toFloat()
                    val circularReveal= ViewAnimationUtils.createCircularReveal(
                            revealView, revealX, revealY,0f, finalRadius)
                    circularReveal.duration = 800
                    circularReveal.interpolator = animation
                    revealView.isVisible = true
                    circularReveal.start()
                    revealView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }

            })
        }
    }
}