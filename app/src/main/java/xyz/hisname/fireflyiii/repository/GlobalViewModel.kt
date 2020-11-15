package xyz.hisname.fireflyiii.repository

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref

class GlobalViewModel(application: Application): AndroidViewModel(application) {

    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener

    val backPress =  MutableLiveData<Boolean>()

    fun handleBackPress(back: Boolean){
        backPress.value = back
    }

    fun isDarkMode(): MutableLiveData<Boolean>{
        val darkModeLiveData: MutableLiveData<Boolean> = MutableLiveData()
        val sharedPreferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val appPreference = AppPref(PreferenceManager.getDefaultSharedPreferences(getApplication()))
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener{ _, key ->
            if (key == "night_mode") {
                if (appPreference.nightModeEnabled) {
                    darkModeLiveData.postValue(true)
                } else {
                    darkModeLiveData.postValue(false)
                }
            }
        }
        sharedPreferenceManager.registerOnSharedPreferenceChangeListener(prefListener)
        if(appPreference.nightModeEnabled){
            darkModeLiveData.postValue(true)
        }
        return darkModeLiveData
    }

}