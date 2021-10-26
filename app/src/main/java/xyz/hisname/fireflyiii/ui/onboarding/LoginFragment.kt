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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.databinding.FragmentLoginBinding
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.getUniqueHash

class LoginFragment: Fragment() {

    private val authViewModel by lazy { getViewModel(AuthActivityViewModel::class.java) }
    private val sharedPref by lazy {  requireContext().getSharedPreferences(
        requireContext().getUniqueHash().toString() + "-user-preferences", Context.MODE_PRIVATE) }
    private var fileUri: Uri? = null
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private var fragmentLoginBinding: FragmentLoginBinding? = null
    private val binding get() = fragmentLoginBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidget()
        getAccessCode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chooseDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()){ fileChoosen ->
            if(fileChoosen != null){
                fileUri = fileChoosen
                binding.certPath.isVisible = true
                binding.certPath.text = FileUtils.getPathFromUri(requireContext(), fileChoosen)
            } else {
                binding.selfSignedCheckbox.isChecked = false
            }

        }
    }

    private fun setWidget(){
        binding.selfSignedCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            AppPref(sharedPref).isCustomCa = isChecked
            if(isChecked){
                openSaf()
            } else {
                binding.certPath.isInvisible = true
            }
        }
        binding.certPath.setOnClickListener {
            openSaf()
        }
    }

    private fun getAccessCode(){
        binding.fireflySubmitButton.setOnClickListener {
            hideKeyboard()
            var fireflyUrl = binding.fireflyUrlEdittext.getString()
            val fireflyId = binding.fireflyIdEdittext.getString()
            val fireflySecretKey =  binding.fireflySecretEdittext.getString()
            val isSuccessful = authViewModel.authViaOauth(fireflyUrl, fireflySecretKey, fireflyId, fileUri)
            if(isSuccessful){
                if (!fireflyUrl.startsWith("http")) {
                    fireflyUrl = "https://$fireflyUrl"
                }
                if(!fireflyUrl.endsWith("/")){
                    fireflyUrl = "$fireflyUrl/"
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, ("$fireflyUrl${Constants.OAUTH_API_ENDPOINT}" +
                        "/authorize?client_id=$fireflyId&redirect_uri=${Constants.REDIRECT_URI}" +
                        "&scope=&response_type=code&state=").toUri())
                browserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                if (browserIntent.resolveActivity(requireContext().packageManager) != null) {
                    requireContext().startActivity(browserIntent)
                }  else {
                    toastError(requireContext().getString(R.string.no_browser_installed))
                }
            }

        }
    }

    // SAF == Storage Access Framework
    // SAF != Singapore Armed Forces
    private fun openSaf(){
        toastInfo("Choose your certificate file", Toast.LENGTH_LONG)
        chooseDocument.launch(arrayOf("application/x-pem-file", "application/pkix-cert"))
    }

}