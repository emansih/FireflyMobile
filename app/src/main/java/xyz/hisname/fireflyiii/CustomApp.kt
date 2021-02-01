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
import org.acra.ACRA
import org.acra.ReportField
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat
import org.acra.sender.EmailIntentSenderFactory

@AcraCore(reportFormat = StringFormat.KEY_VALUE_LIST,
        reportSenderFactoryClasses = [EmailIntentSenderFactory::class], buildConfigClass = BuildConfig::class,
        reportContent = [ReportField.REPORT_ID, ReportField.APP_VERSION_NAME,
        ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.PRODUCT, ReportField.ANDROID_VERSION,
        ReportField.BUILD_CONFIG, ReportField.STACK_TRACE, ReportField.LOGCAT])
@AcraMailSender(reportAsFile = true, mailTo = "", resSubject = R.string.urge_user_to_post_bug_on_github,
        reportFileName = "Fireflyiii-mobile.txt")
class CustomApp: Application() {

    override fun onCreate() {
        super.onCreate()
        newThread()
    }

    private fun newThread(){
        Thread(Runnable {
            if(BuildConfig.DEBUG == false) {
                ACRA.init(this)
            }
        }).start()
    }
}