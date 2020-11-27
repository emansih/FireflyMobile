package xyz.hisname.fireflyiii.util.biometric

import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

internal class Authenticator(private val fragmentActivity: FragmentActivity,
                             private val callback: (AuthenticationResult) -> Unit,
                             private val biometricChecker: BiometricChecker = BiometricChecker.getInstance(fragmentActivity)) {

    private val handler = Handler(Looper.getMainLooper())

    private val biometricCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            callback(AuthenticationResult.UnrecoverableError(errorCode, errString))
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            callback(AuthenticationResult.Failure)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            callback(AuthenticationResult.Success(result.cryptoObject))
        }
    }

    private val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            { runnable -> handler.post(runnable) },
            biometricCallback
    )

    // https://source.android.com/compatibility/11/android-11-cdd#7_3_10_biometric_sensors
    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Prompt")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setNegativeButtonText(fragmentActivity.getString(android.R.string.cancel))
            .build()

    fun authenticate() {
        if (!biometricChecker.hasBiometrics) {
            callback(AuthenticationResult.UnrecoverableError(
                    0,
                    "No biometric hardware found!")
            )
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
    }
}