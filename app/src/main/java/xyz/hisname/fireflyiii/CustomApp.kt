package xyz.hisname.fireflyiii

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.jaredrummler.cyanea.Cyanea


class CustomApp: Application() {


    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Cyanea.init(this, resources)
    }

}