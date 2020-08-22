package xyz.hisname.fireflyiii.receiver

import android.content.Context
import android.os.Bundle
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver

class PluginReceiver: AbstractPluginSettingReceiver(){
    override fun isAsync() = true

    override fun isBundleValid(bundle: Bundle) = true

    override fun firePluginSetting(context: Context, bundle: Bundle) {

    }

}