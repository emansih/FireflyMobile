package xyz.hisname.fireflyiii.ui.base

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import xyz.hisname.fireflyiii.util.LangContextWrapper

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LangContextWrapper.wrap(newBase))
    }

}