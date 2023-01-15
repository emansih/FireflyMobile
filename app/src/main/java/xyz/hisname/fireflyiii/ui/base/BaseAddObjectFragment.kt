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

package xyz.hisname.fireflyiii.ui.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import androidx.core.view.isVisible
import xyz.hisname.fireflyiii.util.animation.BakedBezierInterpolator
import kotlin.math.max

abstract class BaseAddObjectFragment: BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setIcons()
        setWidgets()
    }

    protected fun unReveal(rootView: View){
        val x = rootView.width / 2
        val y = rootView.height / 2
        val finalRadius = (max(rootView.width, rootView.height) * 1.1).toFloat()
        val circularReveal= ViewAnimationUtils.createCircularReveal(
                rootView, x, y,finalRadius, 0f)
        circularReveal.duration = 400
        circularReveal.interpolator = BakedBezierInterpolator.FADE_OUT_CURVE
        circularReveal.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                try {
                    parentFragmentManager.popBackStack()
                } catch(illegal: IllegalStateException){

                }
                rootView.isVisible = false
                fragmentContainer.isVisible = true
            }
        })
        circularReveal.start()
    }

    abstract fun setIcons()
    abstract fun setWidgets()
    abstract fun submitData()

}