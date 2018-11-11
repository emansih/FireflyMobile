package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.GlobalViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel

abstract class BaseFragment: Fragment(), CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.Main
    private val globalViewModel by lazy { getViewModel(GlobalViewModel::class.java) }
    val baseUrl by lazy { globalViewModel.baseUrl.value ?: ""}
    val accessToken by lazy { globalViewModel.accessToken.value ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().findViewById<AppBarLayout>(R.id.activity_appbar)?.setExpanded(true,true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    // Taken from: https://proandroiddev.com/enter-animation-using-recyclerview-and-layoutanimation-part-1-list-75a874a5d213
    fun runLayoutAnimation(recyclerView: RecyclerView){
        val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            layoutAnimation = controller
            adapter?.notifyDataSetChanged()
            scheduleLayoutAnimation()
        }
    }

    override fun onStop() {
        super.onStop()
        coroutineContext.cancel()
    }
}