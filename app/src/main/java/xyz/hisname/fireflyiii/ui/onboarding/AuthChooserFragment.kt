/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentAuthChooserBinding
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError

class AuthChooserFragment: Fragment(){

    private lateinit var authActivityViewModel: AuthActivityViewModel
    private var fragmentAuthChooserBinding: FragmentAuthChooserBinding? = null
    private val binding get() = fragmentAuthChooserBinding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentAuthChooserBinding = FragmentAuthChooserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authActivityViewModel = getViewModel(AuthActivityViewModel::class.java)
        binding.oauthButton.setOnClickListener {
            flipCard(LoginFragment())
        }
        binding.accessTokenButton.setOnClickListener {
            flipCard(PatFragment())
        }
        binding.demoButton.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAuthChooserBinding = null
    }
}