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

package xyz.hisname.fireflyiii

import android.app.Application
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import org.acra.ReportField
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import timber.log.Timber

class CustomApp: Application() {

    override fun onCreate() {
        super.onCreate()
        newThread()
    }

    private fun newThread(){
        Thread {
            if (BuildConfig.DEBUG == false) {
                initAcra {
                    reportFormat = StringFormat.KEY_VALUE_LIST
                    buildConfigClass = BuildConfig::class.java
                    reportContent = listOf(ReportField.REPORT_ID, ReportField.APP_VERSION_NAME,
                        ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.PRODUCT, ReportField.ANDROID_VERSION,
                        ReportField.BUILD_CONFIG, ReportField.STACK_TRACE, ReportField.LOGCAT)
                    mailSender {
                        reportAsFile = true
                        mailTo = ""
                        subject  = getString(R.string.urge_user_to_post_bug_on_github)
                        body = "Hello! I am sorry that the application crashed. " +
                                "Please review the attached log file and post it on" +
                                " Github https://github.com/emansih/FireflyMobile/issues/new. I will " +
                                "try to respond as soon as possible. Thank you!"
                        reportFileName = "PhoturisIII_Bug_Report.txt"
                    }
                }
            } else {
                System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
                Timber.plant(Timber.DebugTree())
            }
        }.start()
    }
}