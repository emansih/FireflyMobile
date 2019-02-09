package xyz.hisname.fireflyiii.util.extension

import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
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

fun RecyclerView.hideFab(floatingActionButton: FloatingActionButton){
    this.addOnScrollListener(object : RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if(dy > 0 && floatingActionButton.isShown){
                floatingActionButton.hide()
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if(newState == RecyclerView.SCROLL_STATE_IDLE){
                floatingActionButton.show()
            }
            super.onScrollStateChanged(recyclerView, newState)
        }
    })
}