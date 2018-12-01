package xyz.hisname.fireflyiii.ui.settings

import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import xyz.hisname.fireflyiii.R
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.util.Base64
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.auth.AuthViewModel
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.ui.onboarding.OnboardingActivity
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import java.util.*


class SettingsFragment: PreferenceFragmentCompat() {

    private val authMethodPref by lazy { accManager.authMethod }
    private val authViewModel by lazy { getViewModel(AuthViewModel::class.java) }
    private val accManager by lazy { AuthenticatorManager(AccountManager.get(requireContext())) }
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_settings)
        setAccountSection()
        setTransactionSection()
    }

    private fun setAccountSection(){
        val fireflyUrlPref = findPreference("fireflyUrl") as EditTextPreference
        fireflyUrlPref.apply {
            title = "Firefly URL"
            summary = AppPref(sharedPref).baseUrl
        }

        val accessTokenPref = findPreference("access_token") as EditTextPreference
        accessTokenPref.apply {
            title = "Access Token"
            summary = accManager.secretKey
        }
        val authMethod = findPreference("auth_method")

        val logout = findPreference("logout")
        if(Objects.equals(authMethodPref, "oauth")){
            authMethod.summary = "OAuth Authentication"
        } else {
            authMethod.summary = "Personal Access Authentication"
        }

        fireflyUrlPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            toastInfo("You should also change your access token", Toast.LENGTH_LONG)
            RetrofitBuilder.destroyInstance()
             true
        }
        accessTokenPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            RetrofitBuilder.destroyInstance()
            true
        }

        logout.setOnPreferenceClickListener {
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                AppDatabase.clearDb(requireContext())
                AppPref(sharedPref).clearPref()
                accManager.destroyAccount()
            }
            val loginActivity = Intent(requireActivity(), OnboardingActivity::class.java)
            startActivity(loginActivity)
            RetrofitBuilder.destroyInstance()
            requireActivity().finish()
            true
        }
        val refreshToken = findPreference("refresh_token")
        refreshToken.setOnPreferenceClickListener {
            toastInfo("Refreshing your token...")
            authViewModel.getRefreshToken().observe(this, Observer { success ->
                if(success){
                    toastSuccess("Token refresh success!")
                } else {
                    toastError("There was an error refreshing your token")
                }
            })
            true
        }
        val certBolean = findPreference("enable_cert_pinning")
        certBolean.setOnPreferenceChangeListener { _, _ ->
            RetrofitBuilder.destroyInstance()
            true
        }
        val certValue = findPreference("cert_value")
        certValue.setOnPreferenceClickListener {
            RetrofitBuilder.destroyInstance()
            true
        }
        certValue.setOnPreferenceChangeListener{ _, newValue ->
            try {
                Base64.decode(newValue.toString(), Base64.DEFAULT).toString(Charsets.UTF_8)
            }catch (exception: IllegalArgumentException){
                AlertDialog.Builder(requireContext())
                        .setTitle("Error parsing Certificate pin value")
                        .setMessage("Your certificate pin is not a valid base64 value. The app will continue" +
                                " to work but you should note that certificate pinning is now useless.")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
            }
            RetrofitBuilder.destroyInstance()
            true
        }
    }

    private fun setTransactionSection(){
        val transactionPref = findPreference("persistent_notification") as CheckBoxPreference
        val notification = NotificationUtils(requireContext())
        transactionPref.setOnPreferenceChangeListener { _, newValue ->
           if(newValue == true){
               notification.showTransactionPersistentNotification()
           } else {
               NotificationManagerCompat.from(requireContext()).cancel("transaction_notif",12345)
           }
            true
        }
    }

    override fun setDivider(divider: Drawable) {
        super.setDivider(ColorDrawable(Color.GRAY))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().activity_toolbar.title = "Settings"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().activity_toolbar.title = "Settings"
    }
}