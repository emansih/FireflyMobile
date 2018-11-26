package xyz.hisname.fireflyiii.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class FireflyAuthenticatorService: Service() {

    override fun onBind(intent: Intent): IBinder {
        val authenticator = FireflyAccountAuthenticator(this)
        return authenticator.iBinder
    }

}