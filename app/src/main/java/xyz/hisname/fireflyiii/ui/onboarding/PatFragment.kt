package xyz.hisname.fireflyiii.ui.onboarding

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_pat.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.TransactionViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*

class PatFragment: Fragment() {

    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val progressOverlay by lazy { requireActivity().findViewById<View>(R.id.progress_overlay) }
    private val model by lazy { getViewModel(TransactionViewModel::class.java) }
    private lateinit var fireflyUrl: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_pat, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fireflySignIn.setOnClickListener {
            hideKeyboard()
            if(firefly_url_edittext.isBlank() or firefly_secret_edittext.isBlank()){
                if(firefly_url_edittext.isBlank()) {
                    firefly_url_edittext.showRequiredError()
                }
                if(firefly_secret_edittext.isBlank()){
                    firefly_secret_edittext.showRequiredError()
                }
            }  else {
                fireflyUrl = if(!firefly_url_edittext.getString().startsWith("http")){
                    "https://${firefly_url_edittext.getString()}"
                } else {
                    firefly_url_edittext.getString()
                }
                if(!fireflyUrl.endsWith("/")){
                    fireflyUrl = "$fireflyUrl/"
                }
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
                model.getTransactions(fireflyUrl,firefly_secret_edittext.getString(), DateTimeUtil.getTodayDate(),
                        DateTimeUtil.getTodayDate(), "withdrawal").apiResponse.observe(this, Observer {
                    apiResponse ->
                    ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
                    if(apiResponse.getError() == null){
                        sharedPref.edit{
                            putString("fireflyUrl", fireflyUrl)
                            putString("fireflySecretKey", firefly_secret_edittext.getString())
                            putString("auth_method", "pat")
                        }
                        val frameLayout = requireActivity().findViewById<FrameLayout>(R.id.bigger_fragment_container)
                        frameLayout.removeAllViews()
                        val bundle = bundleOf("fireflyUrl" to fireflyUrl, "access_token"
                                to firefly_secret_edittext.getString())
                        requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .add(R.id.bigger_fragment_container, OnboardingFragment().apply { arguments = bundle })
                                .commit()
                        toastSuccess(resources.getString(R.string.welcome))
                    } else {
                        toastError(resources.getString(R.string.authentication_failed))
                    }
                })
            }

        }
    }
}