/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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