package xyz.hisname.fireflyiii.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.fragment_auth_chooser.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError

class AuthChooserFragment: Fragment(){

    private lateinit var authActivityViewModel: AuthActivityViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_auth_chooser, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authActivityViewModel = getViewModel(AuthActivityViewModel::class.java)
        oauthButton.setOnClickListener {
            flipCard(LoginFragment())
        }
        accessTokenButton.setOnClickListener {
            flipCard(PatFragment())
        }
        demoButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, ("https://demo.firefly-iii.org/${Constants.OAUTH_API_ENDPOINT}" +
                    "/authorize?client_id=2&redirect_uri=${Constants.DEMO_REDIRECT_URI}" +
                    "&scope=&response_type=code&state=").toUri())
            browserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            if (browserIntent.resolveActivity(requireContext().packageManager) != null){
                requireContext().startActivity(browserIntent)
            } else {
                toastError(resources.getString(R.string.no_browser_installed))
            }
        }
    }


    private fun flipCard(fragment: Fragment){
        if(authActivityViewModel.isShowingBack.value == true){
            parentFragmentManager.popBackStack()
            return
        }
        authActivityViewModel.isShowingBack.postValue(true)
        parentFragmentManager.commit {
            setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
            addToBackStack(null)
            replace(R.id.container, fragment)
        }
    }
}