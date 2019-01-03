package xyz.hisname.fireflyiii.util.extension

import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.display(
        clicky: View.() -> Unit
){
    isVisible = true
    translationY = (6 * 56).toFloat()
    animate().translationY(0f)
            .setInterpolator(OvershootInterpolator(1f))
            .setStartDelay(300)
            .setDuration(400)
            .start()
    this.setOnClickListener(clicky)
}