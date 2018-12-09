package xyz.hisname.fireflyiii.util.animation

import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

class CircularReveal(private val revealView: View) {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun showReveal(revealX: Int, revealY: Int) {
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
                    circularReveal.interpolator = BakedBezierInterpolator.FADE_IN_CURVE
                    revealView.isVisible = true
                    circularReveal.start()
                    revealView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }

            })
        }
    }
}