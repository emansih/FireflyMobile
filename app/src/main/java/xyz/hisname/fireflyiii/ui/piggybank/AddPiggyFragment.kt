package xyz.hisname.fireflyiii.ui.piggybank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.PiggyViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment

class AddPiggyFragment: BaseFragment() {

    private lateinit var model: PiggyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model = ViewModelProviders.of(this)[PiggyViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart(){
        super.onStart()
        activity?.activity_toolbar?.title = "Edit"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Edit"
    }

}