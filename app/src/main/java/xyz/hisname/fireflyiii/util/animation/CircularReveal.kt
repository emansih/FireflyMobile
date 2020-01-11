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