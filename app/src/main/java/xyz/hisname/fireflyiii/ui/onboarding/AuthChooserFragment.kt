package xyz.hisname.fireflyiii.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_auth_chooser.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.create

class AuthChooserFragment: Fragment(){


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_auth_chooser, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        oauthButton.setOnClickListener {
            val bundle = bundleOf("ACTION" to "LOGIN")
            requireFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                    .commit()
        }
        accessTokenButton.setOnClickListener {
            requireFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, PatFragment())
                    .commit()
        }
    }
}