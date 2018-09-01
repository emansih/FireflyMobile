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
                    .setDuration(duration.toLong())
                    .alpha(if (show) toAlpha else 0.toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view.visibility = toVisibility
                        }
                    })
        }
    }
}