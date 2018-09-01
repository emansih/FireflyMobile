package xyz.hisname.fireflyiii

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen



class CustomApp: Application() {


    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }

}