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

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.animation.CircularReveal


abstract class BaseDialog: DialogFragment() {

    private val revealX by lazy { arguments?.getInt("revealX") ?: 0 }
    private val revealY by lazy { arguments?.getInt("revealY") ?: 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidgets()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /*fun unReveal(rootView: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            val x= rootView.width / 2
            val y= rootView.height / 2
            val finalRadius = (Math.max(rootView.width, rootView.height) * 1.1).toFloat()
            val circularReveal= ViewAnimationUtils.createCircularReveal(
                    rootView, x, y,finalRadius, 0f)
            circularReveal.duration = 400
            circularReveal.interpolator = BakedBezierInterpolator.FADE_OUT_CURVE
            circularReveal.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    rootView.isVisible = false
                    dialog?.dismiss()
                }
            })
            circularReveal.start()
        } else {
            dialog?.dismiss()
        }
    }*/

    //fun showReveal(rootLayout: View) = CircularReveal(rootLayout).showReveal(revealX, revealY)


    abstract fun setWidgets()
}