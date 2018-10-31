package xyz.hisname.fireflyiii.ui.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch

// Code taken from: https://geoffreymetais.github.io/code/diffutil-threading/
abstract class DiffUtilAdapter<D, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    protected var dataSet: List<D> = listOf()
    private val diffCallback by lazy(LazyThreadSafetyMode.NONE) { DiffCallback() }
    private val eventActor =
            GlobalScope.actor<List<D>>(Dispatchers.Default, capacity = Channel.CONFLATED, block = {
                for (list in channel) internalUpdate(list)
            })

    fun update (list: List<D>) = eventActor.offer(list)

    private suspend fun internalUpdate(list: List<D>) {
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, null, {
            dataSet = list
            DiffUtil.calculateDiff(diffCallback.apply { newList = list },false)
                    .dispatchUpdatesTo(this@DiffUtilAdapter)
            /* The code taken from Geoffrey Métais didn't call `notifyDataSetChanged()`
            and it resulted in
            `java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item`
            */
            notifyDataSetChanged()
        }).join()
    }

    private inner class DiffCallback : DiffUtil.Callback() {
        lateinit var newList: List<D>
        override fun getOldListSize() = dataSet.size
        override fun getNewListSize() = newList.size
        override fun areContentsTheSame(oldItemPosition : Int, newItemPosition : Int) = true
        override fun areItemsTheSame(oldItemPosition : Int, newItemPosition : Int) =
                dataSet[oldItemPosition] == newList[newItemPosition]
    }
}
