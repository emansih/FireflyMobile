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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Canvas
import android.view.ViewPropertyAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

inline fun ViewPropertyAnimator.onAnimationEnd(crossinline continuation: (Animator) -> Unit) {
    setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            continuation(animation)
        }
    })
}

inline fun RecyclerView.enableDragDrop(fab: ExtendedFloatingActionButton,
                                       crossinline onChildDraw: (viewHolder: RecyclerView.ViewHolder,
                                                                 isCurrentlyActive: Boolean) -> Unit){
    val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or
            ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, 0){

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                fab.animateChange(false)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            fab.animateChange(true)
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            onChildDraw(viewHolder, isCurrentlyActive)
        }
        
    }
    ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(this)
}