package xyz.hisname.fireflyiii.ui.onboarding

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
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_pat.*
import kotlinx.android.synthetic.main.fragment_pat.cert_path
import kotlinx.android.synthetic.main.fragment_pat.firefly_url_edittext
import kotlinx.android.synthetic.main.fragment_pat.self_signed_checkbox
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*

class PatFragment: Fragment() {

    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val authViewModel by lazy { getViewModel(AuthActivityViewModel::class.java) }
    private lateinit var fileUri: Uri
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_pat, container)
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
                cert_path.isVisible = true
                cert_path.text = FileUtils.getPathFromUri(requireContext(), fileUri)
            } else {
                self_signed_checkbox.isChecked = false
            }
        }
    }

    private fun signInButtonClick(){
        fireflySignIn.setOnClickListener {
            hideKeyboard()
            authViewModel.authViaPat(firefly_url_edittext.getString(),
                    firefly_access_edittext.getString(), cert_path.text.toString().toUri())
        }
    }

    private fun setWidgets(){
        self_signed_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            AppPref(sharedPref).isCustomCa = isChecked
            if(isChecked){
                openSaf()
            } else {
                cert_path.isInvisible = true
            }
        }
        cert_path.setOnClickListener {
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
