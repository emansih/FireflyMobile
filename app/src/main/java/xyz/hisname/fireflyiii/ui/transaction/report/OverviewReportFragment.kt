package xyz.hisname.fireflyiii.ui.transaction.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_overview_report.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.create

class OverviewReportFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_overview_report, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.activity_toolbar?.isVisible = false
        setToolbar()
        val adapterViewPager = OverviewPagerAdapter(requireActivity().supportFragmentManager)
        pager.adapter = adapterViewPager
        tabLayout.setupWithViewPager(pager)
    }

    private fun setToolbar(){
        overview_report_toolbar.title = "Report"
        overview_report_toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_left)
        overview_report_toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    override fun onDetach() {
        super.onDetach()
        activity?.activity_toolbar?.isVisible = true
    }
}