package xyz.hisname.fireflyiii.ui.onboarding

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_onboarding.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.UserInfoViewModel
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.zipLiveData

class OnboardingFragment: Fragment() {

    private val model by lazy { getViewModel(UserInfoViewModel::class.java) }
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val baseUrl: String by lazy { arguments?.getString("fireflyUrl") ?: "" }
    private val accessToken: String by lazy { arguments?.getString("access_token") ?: "" }
    private var isThereError = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_onboarding, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUser()
    }

    private fun getUser(){
        ObjectAnimator.ofInt(onboarding_progress,"progress", 10).start()
        zipLiveData(model.getUser(baseUrl,accessToken), model.userSystem(baseUrl,accessToken)).observe(this, Observer { data ->
            if(data.first.getUserData() != null){
                ObjectAnimator.ofInt(onboarding_progress,"progress", 20).start()
                sharedPref.edit{
                    putString("userEmail", data.first.getUserData()?.userData?.userAttributes?.email)
                    putString("userRole", data.first.getUserData()?.userData?.userAttributes?.role)
                }
                ObjectAnimator.ofInt(onboarding_progress,"progress", 50).start()
            } else {
                isThereError = true
            }
            if(data.second.getUserSystem() != null){
                ObjectAnimator.ofInt(onboarding_progress,"progress", 70).start()
                val systemInfo = data.second.getUserSystem()?.systemData
                sharedPref.edit {
                    putString("api_version", systemInfo?.api_version)
                    putString("system_driver", systemInfo?.driver)
                    putString("os", systemInfo?.os)
                    putString("php_version", systemInfo?.php_version)
                    putString("version", systemInfo?.version)
                }
                ObjectAnimator.ofInt(onboarding_progress,"progress", 100).start()
            } else {
                isThereError = true
            }
            if(isThereError){
                toastError("There was some issue retrieving your data")
            } else {
                startActivity(Intent(requireContext(), HomeActivity::class.java))
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                requireActivity().finish()
            }
        })
    }


}