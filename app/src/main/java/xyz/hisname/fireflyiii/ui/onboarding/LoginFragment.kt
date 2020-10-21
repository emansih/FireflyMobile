package xyz.hisname.fireflyiii.ui.onboarding

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
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_login.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*


class LoginFragment: Fragment() {

    private val authViewModel by lazy { getViewModel(AuthActivityViewModel::class.java) }
    private val sharedPref by lazy {  PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private var fileUri: Uri? = null
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_login, container)
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
                cert_path.isVisible = true
                cert_path.text = FileUtils.getPathFromUri(requireContext(), fileChoosen)
            } else {
                self_signed_checkbox.isChecked = false
            }

        }
    }

    private fun setWidget(){
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

    private fun getAccessCode(){
        firefly_submit_button.setOnClickListener {
            hideKeyboard()
            var fireflyUrl = firefly_url_edittext.getString()
            val fireflyId = firefly_id_edittext.getString()
            val fireflySecretKey =  firefly_secret_edittext.getString()
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