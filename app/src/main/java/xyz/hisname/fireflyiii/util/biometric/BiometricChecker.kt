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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.os.Build

sealed class BiometricChecker {

    abstract val hasBiometrics: Boolean

    @TargetApi(Build.VERSION_CODES.Q)
    private class QBiometricChecker(
            private val biometricManager: BiometricManager
    ) : BiometricChecker() {

        private val availableCodes = listOf(
                BiometricManager.BIOMETRIC_SUCCESS,
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        )

        override val hasBiometrics: Boolean
            get() = availableCodes.contains(biometricManager.canAuthenticate())

        companion object {

            fun getInstance(context: Context): QBiometricChecker? =
                    context.getSystemService(BiometricManager::class.java)?.let {
                        QBiometricChecker(it)
                    }
        }
    }

    @Suppress("DEPRECATION")
    private class LegacyBiometricChecker(
            private val fingerprintManager: android.hardware.fingerprint.FingerprintManager
    ) : BiometricChecker() {

        override val hasBiometrics: Boolean
            @SuppressLint("MissingPermission")
            get() = fingerprintManager.isHardwareDetected

        companion object {

            fun getInstance(context: Context): LegacyBiometricChecker? =
                    context.getSystemService(
                            android.hardware.fingerprint.FingerprintManager::class.java
                    )?.let {
                        LegacyBiometricChecker(it)
                    }
        }
    }

    private class DefaultBiometricChecker : BiometricChecker() {

        override val hasBiometrics: Boolean = false
    }

    companion object {

        fun getInstance(context: Context): BiometricChecker {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                    QBiometricChecker.getInstance(context)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    LegacyBiometricChecker.getInstance(context)
                else -> null
            } ?: DefaultBiometricChecker()
        }
    }
}
