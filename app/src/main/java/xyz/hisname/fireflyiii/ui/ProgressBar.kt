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

package xyz.hisname.fireflyiii.ui

import android.view.View
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.core.view.isVisible


class ProgressBar{

    // Code adapted from: https://stackoverflow.com/questions/18021148/display-a-loading-overlay-on-android-screen
    companion object {
        fun animateView(view: View, toVisibility: Int, toAlpha: Float, duration: Int){
            val show: Boolean = toVisibility == View.VISIBLE
            if(show) view.alpha = 0.toFloat()
            view.isVisible = true
            view.bringToFront()
            view.animate()
                    .setDuration(200)
                    .alpha(if (show) toAlpha else 0F)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view.visibility = toVisibility
                        }
                    })
        }
    }
}