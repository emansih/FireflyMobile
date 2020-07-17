package xyz.hisname.fireflyiii.ui.onboarding

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.repository.auth.AuthViewModel
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*


class LoginFragment: Fragment() {

    private val authViewModel by lazy { getViewModel(AuthViewModel::class.java) }
    private val progressOverlay by bindView<View>(R.id.progress_overlay)
    private var baseUrlLiveData: MutableLiveData<String> = MutableLiveData()
    private var clientIdLiveData: MutableLiveData<String> = MutableLiveData()
    private var secretKeyLiveData: MutableLiveData<String> = MutableLiveData()
    private val accManager by lazy { AuthenticatorManager(AccountManager.get(requireContext())) }
    private val sharedPref by lazy {  PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private lateinit var fileUri: Uri

    companion object {
        private const val OPEN_REQUEST_CODE  = 49
        private const val STORAGE_REQUEST_CODE = 7531
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_login, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments?.getString("ACTION")
        setWidget()
        when {
            Objects.equals(argument, "LOGIN") -> {
                baseUrlLiveData.observe(viewLifecycleOwner) {
                    firefly_url_edittext.setText(it)
                }
                clientIdLiveData.observe(viewLifecycleOwner) {
                    firefly_id_edittext.setText(it)
                }
                secretKeyLiveData.observe(viewLifecycleOwner) {
                    firefly_secret_edittext.setText(it)
                }
                getAccessCode()
            }
            Objects.equals(argument, "REFRESH_TOKEN") -> {
                refreshToken()
            }
        }
        authViewModel.isLoading.observe(viewLifecycleOwner) {
            if(it == true){
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun setWidget(){
        firefly_url_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_link
                    sizeDp = 24
                },null, null, null)
        firefly_secret_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_lock
                    sizeDp = 24
                },null, null, null)
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
            FireflyClient.destroyInstance()
            hideKeyboard()
            var fireflyUrl = firefly_url_edittext.getString()
            val fireflyId = firefly_id_edittext.getString()
            val fireflySecretKey =  firefly_secret_edittext.getString()
            if(fireflyUrl.isEmpty() or fireflyId.isEmpty() or fireflySecretKey.isEmpty()){
                when {
                    fireflyUrl.isEmpty() -> firefly_url_edittext.error = resources.getString(R.string.required_field)
                    fireflyId.isEmpty() -> firefly_id_edittext.error = resources.getString(R.string.required_field)
                    else -> firefly_secret_edittext.error = resources.getString(R.string.required_field)
                }
            } else {
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
                if(self_signed_checkbox.isChecked){
                    FileUtils.saveCaFile(fileUri, requireContext())
                }
                baseUrlLiveData.value = fireflyUrl
                AppPref(sharedPref).baseUrl = fireflyUrl
                if (!fireflyUrl.startsWith("http")) {
                    fireflyUrl = "https://$fireflyUrl"
                }
                if(!fireflyUrl.endsWith("/")){
                    fireflyUrl = "$fireflyUrl/"
                }
                clientIdLiveData.value = fireflyId
                secretKeyLiveData.value = fireflySecretKey
                val browserIntent = Intent(Intent.ACTION_VIEW, ("$fireflyUrl${Constants.OAUTH_API_ENDPOINT}" +
                        "/authorize?client_id=$fireflyId&redirect_uri=${Constants.REDIRECT_URI}" +
                        "&scope=&response_type=code&state=").toUri())
                browserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                if (browserIntent.resolveActivity(requireActivity().packageManager) != null){
                    startActivity(browserIntent)
                } else {
                    toastError(resources.getString(R.string.no_browser_installed))
                }
            }
        }
    }

    private fun refreshToken(){
        rootLayout.isVisible = false
        toastInfo(resources.getString(R.string.refreshing_token), Toast.LENGTH_LONG)
        authViewModel.getRefreshToken().observe(viewLifecycleOwner) {
            if(it == true){
                startHomeIntent()
            } else {
                toastError(resources.getString(R.string.issue_refreshing_token))
            }
        }
    }

    private fun startHomeIntent(){
        if(AppPref(sharedPref).isTransactionPersistent){
            NotificationUtils(requireContext()).showTransactionPersistentNotification()
        }
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            delay(500)
            startActivity(Intent(requireContext(), HomeActivity::class.java))
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            requireActivity().finish()
        }
    }

    private fun showDialog(){
        ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.authentication_failed))
                .setMessage(resources.getString(R.string.authentication_failed_message, Constants.REDIRECT_URI))
                .setPositiveButton("OK") { _, _ ->
                }
                .create()
                .show()
    }

    // SAF == Storage Access Framework
    // SAF != Singapore Armed Forces
    private fun openSaf(){
        toastInfo("Import your cert", Toast.LENGTH_LONG)
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else {
            val mimeTypes = arrayOf("application/x-pem-file", "application/pkix-cert")
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = mimeTypes[0]
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), OPEN_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        val uri = requireActivity().intent.data
        if(uri != null && uri.toString().startsWith(Constants.REDIRECT_URI)){
            ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
            hideKeyboard()
            val code = uri.getQueryParameter("code")
            if(code != null && code.isNotBlank() && code.isNotEmpty()) {
                accManager.initializeAccount()
                accManager.apply {
                    secretKey = secretKeyLiveData.value ?: ""
                    clientId = clientIdLiveData.value ?: ""
                }
                authViewModel.getAccessToken(code).observe(viewLifecycleOwner) { isAuth ->
                    if(isAuth){
                        val layout = requireActivity().findViewById<ConstraintLayout>(R.id.small_container)
                        layout.isVisible = false
                        toastSuccess(resources.getString(R.string.welcome))
                        ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
                        parentFragmentManager.commit {
                            setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            replace(R.id.bigger_fragment_container, OnboardingFragment())
                        }
                    } else {
                        ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
                        authViewModel.authFailedReason.observe(viewLifecycleOwner) { reason ->
                            if(reason.isNotBlank()){
                                toastError(reason, Toast.LENGTH_LONG)
                            }
                        }
                    }
                }
            } else {
                showDialog()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.size == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSaf()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if(resultCode == Activity.RESULT_OK){
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    fileUri = resultData.data?: Uri.EMPTY
                    cert_path.isVisible = true
                    cert_path.text = FileUtils.getPathFromUri(requireContext(), fileUri)
                } else {
                    self_signed_checkbox.isChecked = false
                }
            }
        }
    }
}