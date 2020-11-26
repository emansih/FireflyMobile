package xyz.hisname.fireflyiii.repository.summary

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import org.json.JSONObject
import xyz.hisname.fireflyiii.data.local.pref.SimpleData
import xyz.hisname.fireflyiii.data.remote.firefly.api.SummaryService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.util.network.retrofitCallback

class SummaryViewModel(application: Application): BaseViewModel(application) {

    val networthValue: MutableLiveData<String> = MutableLiveData()
    val leftToSpendValue: MutableLiveData<String> = MutableLiveData()
    val balanceValue: MutableLiveData<String> = MutableLiveData()
    val earnedValue: MutableLiveData<String> = MutableLiveData()
    val spentValue: MutableLiveData<String> = MutableLiveData()
    val billsToPay: MutableLiveData<String> = MutableLiveData()
    val billsPaid: MutableLiveData<String> = MutableLiveData()
    val leftToSpendDay: MutableLiveData<String> = MutableLiveData()

}