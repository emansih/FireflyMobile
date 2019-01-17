package xyz.hisname.fireflyiii.ui.settings

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import xyz.hisname.fireflyiii.R
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.commit
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.ui.onboarding.OnboardingActivity
import xyz.hisname.languagepack.LanguageChanger


class SettingsFragment: BaseSettings() {

    private val accManager by lazy { AuthenticatorManager(AccountManager.get(requireContext())) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_settings)
        setAccountSection()
        setTransactionSection()
        setLanguagePref()
    }

    private fun setLanguagePref(){
        val languagePref = findPreference("language_pref") as ListPreference
        languagePref.summary = languagePref.entry
        languagePref.setOnPreferenceChangeListener { _, newValue ->
            AppPref(sharedPref).languagePref = newValue.toString()
            LanguageChanger.init(requireContext(), AppPref(sharedPref).languagePref)
            requireActivity().recreate()
            true
        }
    }

    private fun setAccountSection(){
       val logout = findPreference("logout")
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
       val accountMoreOptions = findPreference("account_more_options")
       accountMoreOptions.setOnPreferenceClickListener {
           requireFragmentManager().commit {
               addToBackStack(null)
               setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
               replace(R.id.fragment_container, SettingsAccountFragment())
           }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Settings"
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = "Settings"
    }
}