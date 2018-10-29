package xyz.hisname.fireflyiii.ui.onboarding

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_onboarding.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.viewmodel.UserInfoViewModel
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.zipLiveData

class OnboardingFragment: Fragment() {

    private val model by lazy { getViewModel(UserInfoViewModel::class.java) }
    private val baseUrl: String by lazy { arguments?.getString("fireflyUrl") ?: "" }
    private val accessToken: String by lazy { arguments?.getString("access_token") ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_onboarding, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUser()
    }

    private fun getUser(){
        RetrofitBuilder.destroyInstance()
        ObjectAnimator.ofInt(onboarding_progress,"progress", 30).start()
        zipLiveData(model.getUser(baseUrl,accessToken), model.userSystem(baseUrl, accessToken))
                .observe(this, Observer {
            if(it.first.getError() == null && it.second.getError() == null){
                ObjectAnimator.ofInt(onboarding_progress,"progress", 50).start()
                ObjectAnimator.ofInt(onboarding_progress,"progress", 90).start()
                startActivity(Intent(requireActivity(), HomeActivity::class.java))
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                requireActivity().finish()
            }else {
                if(it.first.getError()!!.localizedMessage.startsWith("Unable to resolve host")){
                    toastError(resources.getString(R.string.unable_ping_server))
                } else {
                    toastError("There was some issue retrieving your data")
                }
            }
        })
    }


}