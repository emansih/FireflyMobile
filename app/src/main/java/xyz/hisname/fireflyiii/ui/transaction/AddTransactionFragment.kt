package xyz.hisname.fireflyiii.ui.transaction

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.animation.BakedBezierInterpolator
import xyz.hisname.fireflyiii.util.animation.CircularReveal
import xyz.hisname.fireflyiii.util.extension.create

class AddTransactionFragment: BaseFragment() {

    private val fab by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.globalFAB) }
    private val fragmentContainer by lazy { requireActivity().findViewById<FrameLayout>(R.id.fragment_container) }
    private val revealX by lazy { arguments?.getInt("revealX") ?: 0 }
    private val revealY by lazy { arguments?.getInt("revealY") ?: 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fab.isGone = true
        requireActivity().findViewById<FrameLayout>(R.id.fragment_container).isVisible = false
        return inflater.create(R.layout.fragment_add_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CircularReveal(fragment_add_transaction_root).showReveal(revealX, revealY, BakedBezierInterpolator.TRANSFORM_CURVE)
        transactionViewPager.adapter = TransactionPagerAdapter(requireFragmentManager(), requireContext())
        transactionTabLayout.setupWithViewPager(transactionViewPager)
        placeHolderToolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.abc_ic_clear_material)
        placeHolderToolbar.setNavigationOnClickListener { unReveal(fragment_add_transaction_root) }

    }

    private fun unReveal(rootView: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            val x= rootView.width / 2
            val y= rootView.height / 2
            val finalRadius = (Math.max(rootView.width, rootView.height) * 1.1).toFloat()
            val circularReveal= ViewAnimationUtils.createCircularReveal(
                    rootView, x, y,finalRadius, 0f)
            circularReveal.duration = 400
            circularReveal.interpolator = BakedBezierInterpolator.FADE_OUT_CURVE
            circularReveal.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    requireFragmentManager().popBackStack()
                    rootView.isVisible = false
                }
            })
            circularReveal.start()
        } else {
            requireFragmentManager().popBackStack()
        }
    }

    override fun onStop() {
        super.onStop()
        fragmentContainer.isVisible = true
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentContainer.isVisible = true
        fab.isVisible = true
    }

}