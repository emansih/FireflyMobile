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

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import xyz.hisname.fireflyiii.databinding.FragmentPatBinding
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*

class PatFragment: Fragment() {

    private val authViewModel by lazy { getViewModel(AuthActivityViewModel::class.java) }
    private var fileUri: Uri? = null
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private var fragmentPatBinding: FragmentPatBinding? = null
    private val binding get() = fragmentPatBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentPatBinding = FragmentPatBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidgets()
        signInButtonClick()
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

    private fun signInButtonClick(){
        binding.fireflySignIn.setOnClickListener {
            hideKeyboard()
            authViewModel.baseUrl.postValue(binding.fireflyUrlEdittext.getString())
            authViewModel.authViaPat(binding.fireflyUrlEdittext.getString(),
                    binding.fireflyAccessEdittext.getString(), fileUri)
        }
    }

    private fun setWidgets(){
        binding.selfSignedCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
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

    // SAF == Storage Access Framework
    // SAF != Singapore Armed Forces
    private fun openSaf(){
        toastInfo("Choose your certificate file", Toast.LENGTH_LONG)
        chooseDocument.launch(arrayOf("application/x-pem-file", "application/pkix-cert"))
    }

}
