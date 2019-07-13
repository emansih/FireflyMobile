package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R

abstract class BaseDetailFragment: BaseFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

    override fun onResume() {
        super.onResume()
        fab.isVisible = false
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

    override fun onStop() {
        super.onStop()
        fab.isVisible = true
    }

    abstract fun deleteItem()
}