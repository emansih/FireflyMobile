package xyz.hisname.fireflyiii.repository.account

import android.app.Application
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

open class BaseAccountViewModel(application: Application): BaseViewModel(application) {

    val repository: AccountRepository
    var accountData: MutableList<AccountData>? = null

    init {
        val accountDao = AppDatabase.getInstance(application).accountDataDao()
        repository = AccountRepository(accountDao)
    }

}