package xyz.hisname.fireflyiii.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.ui.HomeActivity
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import java.util.*


class OnboardingActivity: AppCompatActivity() {

    private val fireflyUrl by lazy { sharedPref.getString("fireflyUrl","") ?: "" }
    private val fireflySecretKey by lazy { sharedPref.getString("fireflySecretKey","") ?: "" }
    private val fireflyAccessTokenExpiry by lazy { sharedPref.getLong("expires_at",0) }
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val authMethod  by lazy { sharedPref.getString("auth_method","") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        when {
            fireflyUrl.isEmpty() or fireflySecretKey.isEmpty() -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AuthChooserFragment())
                        .commit()
            }
            authMethod.isNotEmpty() -> {
                when {
                    Objects.equals("oauth", authMethod) -> {
                        if (System.currentTimeMillis() > fireflyAccessTokenExpiry) {
                            val bundle = bundleOf("ACTION" to "REFRESH_TOKEN")
                            supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                                    .commit()
                        } else {
                            if (sharedPref.getBoolean("persistent_notification", false)) {
                                NotificationUtils(this).showTransactionPersistentNotification()
                            }
                            GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                                delay(500)
                                startActivity(Intent(this@OnboardingActivity, HomeActivity::class.java))
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                finish()
                            }
                        }
                    }
                }
            }
            else -> {
                if(sharedPref.getBoolean("persistent_notification",false)){
                    NotificationUtils(this).showTransactionPersistentNotification()
                }
                if(fireflyUrl.isNotEmpty() and fireflySecretKey.isNotEmpty() and authMethod.isNotEmpty()) {
                    GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                        delay(1234) //heh
                        startActivity(Intent(this@OnboardingActivity, HomeActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                } else {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, AuthChooserFragment())
                            .commit()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        RetrofitBuilder.destroyInstance()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)

    }
}

