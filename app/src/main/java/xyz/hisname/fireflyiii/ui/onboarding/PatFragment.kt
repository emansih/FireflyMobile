package xyz.hisname.fireflyiii.ui.onboarding

import android.accounts.AccountManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_pat.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.firefly.FireflyClient
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.AddAccountFragment
import xyz.hisname.fireflyiii.util.extension.*

class PatFragment: Fragment() {

    private val progressOverlay by bindView<View>(R.id.progress_overlay)
    private val model by lazy { getViewModel(AccountsViewModel::class.java) }
    private lateinit var fireflyUrl: String
    private val accountManager by lazy { AccountManager.get(requireContext()) }
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_pat, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firefly_url_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_link)
                        .sizeDp(24),null, null, null)
        firefly_access_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_lock)
                        .sizeDp(24),null, null, null)
        fireflySignIn.setOnClickListener {
            hideKeyboard()
            if(firefly_url_edittext.isBlank() or firefly_access_edittext.isBlank()){
                if(firefly_url_edittext.isBlank()) {
                    firefly_url_layout.showRequiredError()
                }
                if(firefly_access_edittext.isBlank()){
                    firefly_access_layout.showRequiredError()
                }
            }  else {
                ProgressBar.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
                FireflyClient.destroyInstance()
                AuthenticatorManager(accountManager).destroyAccount()
                fireflyUrl = firefly_url_edittext.getString()
                AuthenticatorManager(accountManager).initializeAccount()
                AppPref(sharedPref).baseUrl = fireflyUrl
                AuthenticatorManager(accountManager).accessToken = firefly_access_edittext.getString().trim()
                model.getAccountByType("asset").observe(this) { accountData ->
                    ProgressBar.animateView(progressOverlay, View.GONE, 0f, 200)
                    if(accountData.isNotEmpty()){
                        AuthenticatorManager(AccountManager.get(requireContext())).authMethod = "pat"
                        val layout = requireActivity().findViewById<ConstraintLayout>(R.id.small_container)
                        layout.isVisible = false
                        requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .add(R.id.bigger_fragment_container, OnboardingFragment())
                                .commit()
                        toastSuccess(resources.getString(R.string.welcome))
                    } else {
                        model.apiResponse.observe(this){ response ->
                            if(response.isNotEmpty()){
                                toastError(response)
                            } else {
                                AlertDialog.Builder(requireContext())
                                        .setTitle("No asset accounts found!")
                                        .setMessage("We tried searching for an asset account but is unable to find any. Would you like" +
                                                "to add an asset account first? ")
                                        .setPositiveButton("OK"){ _,_ ->
                                            requireFragmentManager().commit {
                                                replace(R.id.bigger_fragment_container, AddAccountFragment())
                                                arguments = bundleOf("accountType" to "asset")
                                            }
                                        }
                                        .setNegativeButton("No"){ _,_ ->
                                            AppPref(sharedPref).clearPref()
                                            AuthenticatorManager(accountManager).destroyAccount()
                                            requireActivity().finish()
                                        }
                                        .setCancelable(false)
                                        .show()
                            }
                        }

                    }
                }
            }
        }
    }
}
