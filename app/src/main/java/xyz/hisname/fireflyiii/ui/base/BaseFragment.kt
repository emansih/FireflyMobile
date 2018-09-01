package xyz.hisname.fireflyiii.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import xyz.hisname.fireflyiii.R

abstract class BaseFragment: Fragment(){

    val baseUrl: String by lazy { arguments?.getString("fireflyUrl") ?: "" }
    val accessToken: String by lazy { arguments?.getString("access_token") ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val appBar = requireActivity().findViewById<AppBarLayout>(R.id.activity_appbar)
        appBar.setExpanded(true,true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}