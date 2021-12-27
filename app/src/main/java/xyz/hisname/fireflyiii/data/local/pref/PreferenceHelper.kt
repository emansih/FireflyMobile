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

package xyz.hisname.fireflyiii.data.local.pref

import androidx.work.NetworkType

interface PreferenceHelper {

    var baseUrl: String
    var isTransactionPersistent: Boolean
    var userRole: String
    var remoteApiVersion: String
    var serverVersion: String
    var userOs: String
    var certValue: String
    var languagePref: String
    var nightModeEnabled: Boolean
    var isKeyguardEnabled: Boolean
    var isCurrencyThumbnailEnabled: Boolean
    var workManagerDelay: Long
    var workManagerLowBattery: Boolean
    var workManagerNetworkType: NetworkType
    var workManagerRequireCharging: Boolean
    var budgetIssue4394: Boolean
    var dateTimeFormat: Int
    var userDefinedDateTimeFormat: String
    var userDefinedDownloadDirectory: String
    fun clearPref()
}