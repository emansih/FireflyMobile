package xyz.hisname.fireflyiii.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.RetrofitBuilder


class OnboardingActivity: AppCompatActivity() {

    private val fireflyUrl by lazy { sharedPref.getString("fireflyUrl","") ?: "" }
    private val fireflyId by lazy { sharedPref.getString("fireflyId","") ?: ""}
    private val fireflySecretKey by lazy { sharedPref.getString("fireflySecretKey","") ?: "" }
    private val fireflyAccessTokenExpiry by lazy { sharedPref.getLong("expires_at",0) }
    private val userEmail by lazy { sharedPref.getString("userEmail","") ?: ""}
    private val userRole by lazy { sharedPref.getString("userRole", "") ?: "" }
    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        when {
            fireflyUrl.isEmpty() or fireflyId.isEmpty() or fireflySecretKey.isEmpty()
                    or(fireflyAccessTokenExpiry == 0L) -> {
                val bundle = bundleOf("ACTION" to "LOGIN")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                        .commit()
            }
            System.currentTimeMillis() > fireflyAccessTokenExpiry -> {
                val bundle = bundleOf("ACTION" to "REFRESH_TOKEN")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                        .commit()
            }
            userEmail.isEmpty() or userRole.isEmpty() -> {
                val bundle = bundleOf("fireflyUrl" to fireflyUrl, "access_token" to sharedPref.getString("access_token",""))
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, OnboardingFragment().apply { arguments = bundle })
                        .commit()
            }
            else -> {
                val bundle = bundleOf("ACTION" to "HOME")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment().apply { arguments = bundle })
                        .commit()
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
}

