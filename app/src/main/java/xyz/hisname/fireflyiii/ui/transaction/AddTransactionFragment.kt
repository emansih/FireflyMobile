package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.animation.BakedBezierInterpolator
import xyz.hisname.fireflyiii.util.animation.CircularReveal
import xyz.hisname.fireflyiii.util.extension.create

class AddTransactionFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.create(R.layout.fragment_add_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CircularReveal(fragment_add_transaction_root).showReveal(revealX, revealY, BakedBezierInterpolator.TRANSFORM_CURVE)
        transactionViewPager.adapter = TransactionPagerAdapter(requireFragmentManager(), requireContext())
        transactionTabLayout.setupWithViewPager(transactionViewPager)
        placeHolderToolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.abc_ic_clear_material)
        placeHolderToolbar.setNavigationOnClickListener {
            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            fragment_add_transaction_root.isVisible = false
            requireFragmentManager().popBackStack()
            fragmentContainer.isVisible = true
            fab.isVisible = true
        }

    }

}