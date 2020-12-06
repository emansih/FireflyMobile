package xyz.hisname.fireflyiii.ui.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.animation.BakedBezierInterpolator
import xyz.hisname.fireflyiii.util.animation.CircularReveal
import xyz.hisname.fireflyiii.util.extension.getViewModel


abstract class BaseDialog: DialogFragment() {

    private val revealX by lazy { arguments?.getInt("revealX") ?: 0 }
    private val revealY by lazy { arguments?.getInt("revealY") ?: 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidgets()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /*fun unReveal(rootView: View){
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
    }*/

    fun showReveal(rootLayout: View) = CircularReveal(rootLayout).showReveal(revealX, revealY)


    abstract fun setWidgets()
}