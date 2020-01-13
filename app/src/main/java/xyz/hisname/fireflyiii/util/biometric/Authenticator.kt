package xyz.hisname.fireflyiii.util.biometric

import android.os.Handler
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

internal class Authenticator(private val fragmentActivity: FragmentActivity,
                             private val callback: (AuthenticationResult) -> Unit,
                             private val biometricChecker: BiometricChecker = BiometricChecker.getInstance(fragmentActivity)) {

    private val handler = Handler()

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

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Prompt")
            .setNegativeButtonText("Cancel")
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