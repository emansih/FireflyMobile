package xyz.hisname.fireflyiii

import android.content.Context
import android.preference.PreferenceManager
import androidx.multidex.MultiDexApplication
import com.jakewharton.threetenabp.AndroidThreeTen
import org.acra.ACRA
import org.acra.ReportField
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat
import org.acra.sender.EmailIntentSenderFactory
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.languagepack.LanguageChanger

@AcraCore(reportFormat = StringFormat.KEY_VALUE_LIST,
        reportSenderFactoryClasses = [EmailIntentSenderFactory::class], buildConfigClass = BuildConfig::class,
        reportContent = [ReportField.REPORT_ID, ReportField.APP_VERSION_NAME,
        ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.PRODUCT, ReportField.ANDROID_VERSION,
        ReportField.BUILD_CONFIG, ReportField.STACK_TRACE, ReportField.LOGCAT])
@AcraMailSender(reportAsFile = true, mailTo = "", resSubject = R.string.urge_user_to_post_bug_on_github,
        reportFileName = "Fireflyiii-mobile.txt")
class CustomApp: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        newThread()
    }

    private fun newThread(){
        Thread(Runnable {
            AndroidThreeTen.init(this, "2019a-TZDB.dat")
            if(BuildConfig.DEBUG == false) {
                ACRA.init(this)
            }
        }).start()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageChanger.init(newBase,
                AppPref(PreferenceManager.getDefaultSharedPreferences(newBase)).languagePref))
    }
}