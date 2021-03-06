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

package xyz.hisname.fireflyiii.util.extension

import android.graphics.drawable.AnimatedVectorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R

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

fun ExtendedFloatingActionButton.display(clicky: View.() -> Unit){
    isVisible = true
    translationY = (6 * 56).toFloat()
    animate().translationY(0f)
            .setInterpolator(OvershootInterpolator(1f))
            .setStartDelay(300)
            .setDuration(400)
            .start()
    this.setOnClickListener(clicky)
}

fun ExtendedFloatingActionButton.animateChange(isAdd: Boolean){
    val colorRemove = getCompatColor(R.color.md_red_600)
    val colorAdd = getCompatColor(R.color.colorAccent)
    val animationAddToDelete = this.context.getCompatDrawable(R.drawable.ic_add_to_delete) as AnimatedVectorDrawable
    val animationDeleteToAdd  = this.context.getCompatDrawable(R.drawable.ic_delete_to_add) as AnimatedVectorDrawable
    if(isAdd){
        this.apply {
            text = "Add"
            icon = animationDeleteToAdd
            animationDeleteToAdd.start()
            animateBackgroundStateChange(colorRemove, colorAdd)
            isClickable = true
            isFocusable = true
        }
    } else {
        this.apply {
            text = "Remove"
            icon = animationAddToDelete
            animationAddToDelete.start()
            animateBackgroundStateChange(colorAdd, colorRemove)
            isClickable = false
            isFocusable = false
        }
    }
    TransitionManager.beginDelayedTransition(this.parent as ViewGroup)
}

fun ExtendedFloatingActionButton.dropToRemove(){
    this.apply {
        text = "Drop to remove"
        icon = IconicsDrawable(this.context, GoogleMaterial.Icon.gmd_delete).apply {
            sizeDp = 24
        }
        isClickable = false
        isFocusable = false
    }
}