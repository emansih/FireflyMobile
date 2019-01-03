package xyz.hisname.fireflyiii

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat
import org.acra.sender.EmailIntentSenderFactory
import xyz.hisname.languagepack.LanguageChanger

@AcraCore(reportFormat = StringFormat.KEY_VALUE_LIST,
        reportSenderFactoryClasses = [EmailIntentSenderFactory::class], buildConfigClass = BuildConfig::class)
@AcraMailSender(reportAsFile = true, mailTo = "", resSubject = R.string.app_name,
        reportFileName = "Fireflyiii-mobile.txt")
class CustomApp: Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        LanguageChanger.init(this)
        ACRA.init(this)
    }

}