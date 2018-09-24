package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R

abstract class BaseDetailFragment: BaseFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

    abstract fun deleteItem()
}