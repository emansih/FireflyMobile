package xyz.hisname.fireflyiii.repository.viewmodel.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData

class DaoPiggyViewModel: AndroidViewModel {

    private lateinit var piggyList: LiveData<MutableList<PiggyData>>

    constructor(application: Application) : super(application)

    fun getPiggyBank(): LiveData<MutableList<PiggyData>>{
        piggyList = AppDatabase.getInstance(getApplication())?.piggyDataDao()?.getPiggy()!!
        return piggyList
    }

    fun deletePiggyBank(data: Long): Int{
        return AppDatabase.getInstance(getApplication())?.piggyDataDao()?.deletePiggyById(data)!!
    }

}