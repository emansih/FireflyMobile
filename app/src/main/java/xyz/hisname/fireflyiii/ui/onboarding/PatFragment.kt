package xyz.hisname.fireflyiii.ui.onboarding

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_pat.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.util.extension.*

class PatFragment: Fragment() {

    private val progressOverlay by bindView<View>(R.id.progress_overlay)
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val patViewModel by lazy { getImprovedViewModel(PatViewModel::class.java) }

    companion object {
        private const val OPEN_REQUEST_CODE  = 42
        private const val STORAGE_REQUEST_CODE = 7331
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_pat, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidgets()
        signInButtonClick()

    }

    private fun signInButtonClick(){
        fireflySignIn.setOnClickListener {
            hideKeyboard()
            if(firefly_url_edittext.isBlank() or firefly_access_edittext.isBlank()) {
                if(firefly_url_edittext.isBlank()) {
                    firefly_url_layout.showRequiredError()
                }
                if(firefly_access_edittext.isBlank()){
                    firefly_access_layout.showRequiredError()
                }
            } else {
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
                patViewModel.authenticate(cert_path.text.toString().toUri(), firefly_access_edittext.getString(),
                        firefly_url_edittext.getString()).observe(this) { auth ->
                    ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
                    if (auth) {
                        val layout = requireActivity().findViewById<ConstraintLayout>(R.id.small_container)
                        layout.isVisible = false
                        requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .add(R.id.bigger_fragment_container, OnboardingFragment())
                                .commit()
                        toastSuccess(resources.getString(R.string.welcome))
                    } else {
                        patViewModel.apiResponse.observe(this) { message ->
                            if (message != null) {
                                toastError(message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setWidgets(){
        firefly_url_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_link
                    sizeDp = 24
                },null, null, null)
        firefly_access_edittext.setCompoundDrawablesWithIntrinsicBounds(
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

    // SAF == Storage Access Framework
    // SAF != Singapore Armed Forces
    private fun openSaf(){
        toastInfo("Choose your certificate file", Toast.LENGTH_LONG)
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else {
            val documentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/x-pem-file"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(documentIntent, OPEN_REQUEST_CODE)
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
                    cert_path.isVisible = true
                    cert_path.text = patViewModel.getFilePath(resultData.data)
                }
            }
        }
    }
}
