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

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.databinding.FragmentOnboardingBinding
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.util.extension.*

class OnboardingFragment: Fragment() {

    private val authActivityViewModel by lazy { getViewModel(AuthActivityViewModel::class.java) }
    private var fragmentOnboardingBinding: FragmentOnboardingBinding? = null
    private val binding get() = fragmentOnboardingBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentOnboardingBinding = FragmentOnboardingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingText.text = "Hang on..."
        hideKeyboard()
        ObjectAnimator.ofInt(binding.onboardingProgress,"progress", 10).start()
        FireflyClient.destroyInstance()
        ObjectAnimator.ofInt(binding.onboardingProgress,"progress", 30).start()
        getUser()
    }


    private fun getUser(){
        binding.onboardingText.text = "Retrieving your data..."
        ObjectAnimator.ofInt(binding.onboardingProgress,"progress", 45).start()
        ObjectAnimator.ofInt(binding.onboardingProgress,"progress", 60).start()
        zipLiveData(authActivityViewModel.getUser(),authActivityViewModel.userSystem()).observe(viewLifecycleOwner) { multipleLiveData ->
            ObjectAnimator.ofInt(binding.onboardingProgress,"progress", 90).start()
            binding.onboardingText.text = "Almost there!"
            if(multipleLiveData.first && multipleLiveData.second){
                startActivity(Intent(requireActivity(), HomeActivity::class.java))
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                requireActivity().finish()
            }
        }
    }

}