/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.account.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.AccountsService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.account.AccountRepository
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

class AccountSearchViewModel(application: Application): BaseViewModel(application) {

    private val accountDao = AppDatabase.getInstance(application, getUniqueHash()).accountDataDao()
    private val accountService = genericService().create(AccountsService::class.java)
    private val accountRepository = AccountRepository(accountDao, accountService)

    val accountName = MutableLiveData<String>()

    fun searchAccount(query: String, accountType: String): LiveData<PagingData<AccountData>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE, enablePlaceholders = false)){
            accountRepository.accountSearchPageSource(query, accountType)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }

    fun getAccountList(accountType: String): LiveData<PagingData<AccountData>>{
        return Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)){
            accountRepository.getAccountList(accountType)
        }.flow.cachedIn(viewModelScope).asLiveData()
    }
}