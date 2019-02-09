package xyz.hisname.fireflyiii.util.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.ViewPropertyAnimator
import android.view.animation.Animation

inline fun ViewPropertyAnimator.onAnimationEnd(crossinline continuation: (Animator) -> Unit) {
    setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            continuation(animation)
        }
    })
}

fun Animation.onAnimationEnd(onAnimationEnd: () -> Unit) {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(p0: Animation) {}
        override fun onAnimationEnd(p0: Animation) {onAnimationEnd()}
        override fun onAnimationStart(p0: Animation) {}
    })
}