package xyz.hisname.fireflyiii.ui.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.animation.BakedBezierInterpolator

open class BaseDialog: DialogFragment() {

    protected val navIcon by lazy { ContextCompat.getDrawable(requireContext(), R.drawable.abc_ic_clear_material) }
    protected val revealX by lazy { arguments?.getInt("revealX") ?: 0 }
    protected val revealY by lazy { arguments?.getInt("revealY") ?: 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun unReveal(rootView: View){
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
                    rootView.isVisible = false
                    dialog?.dismiss()
                }
            })
            circularReveal.start()
        } else {
            dialog?.dismiss()
        }
    }
}