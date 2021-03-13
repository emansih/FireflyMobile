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

package xyz.hisname.fireflyiii.ui.tags

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import xyz.hisname.fireflyiii.BuildConfig
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TagsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.tags.TagsData
import xyz.hisname.fireflyiii.repository.models.transaction.SplitSeparator
import xyz.hisname.fireflyiii.repository.tags.TagsRepository
import xyz.hisname.fireflyiii.repository.transaction.TransactionRepository
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.insertDateSeparator
import xyz.hisname.fireflyiii.util.network.HttpConstants
import java.io.File

class TagDetailsViewModel(application: Application): BaseViewModel(application) {

    private val tagsRepository = TagsRepository(
            AppDatabase.getInstance(application).tagsDataDao(),
            genericService().create(TagsService::class.java)
    )

    private val transactionRepository = TransactionRepository(
            AppDatabase.getInstance(application).transactionDataDao(),
            genericService().create(TransactionService::class.java)
    )

    private val currencyRepository = CurrencyRepository(AppDatabase.getInstance(application).currencyDataDao(),
            genericService().create(CurrencyService::class.java)
    )


    val transactionList = MutableLiveData<PagingData<SplitSeparator>>()
    val transactionSum = MutableLiveData<ArrayList<DetailModel>>()

    init {
        Configuration.getInstance().load(application, PreferenceManager.getDefaultSharedPreferences(application))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = application.filesDir
        Configuration.getInstance().osmdroidTileCache = File(application.filesDir.toString() + "/tiles")
    }

    fun getTagById(tagId: Long): LiveData<TagsData> {
        val tagData = MutableLiveData<TagsData>()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            val tagAttribute = tagsRepository.getTagById(tagId)
            tagData.postValue(tagAttribute)
            Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
                transactionRepository.getTransactionByTagAndDate(DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getEndOfMonth(), tagAttribute.tagsAttributes.description)
            }.flow.insertDateSeparator().cachedIn(viewModelScope).collectLatest { pagingData ->
                transactionList.postValue(pagingData)
                getSumDetails(tagAttribute.tagsAttributes.description)
                isLoading.postValue(false)
            }
            isLoading.postValue(false)
        }
        return tagData
    }

    fun getTagByName(nameOfTag: String): LiveData<TagsData>{
        val tagData: MutableLiveData<TagsData> = MutableLiveData()
        isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO){
            tagData.postValue(tagsRepository.getTagByName(nameOfTag))
            Pager(PagingConfig(pageSize = Constants.PAGE_SIZE)) {
                transactionRepository.getTransactionByTagAndDate(DateTimeUtil.getStartOfMonth(),
                        DateTimeUtil.getEndOfMonth(), nameOfTag)
            }.flow.insertDateSeparator().cachedIn(viewModelScope).collectLatest { pagingData ->
                transactionList.postValue(pagingData)
                getSumDetails(nameOfTag)
                isLoading.postValue(false)
            }
        }
        return tagData
    }

    fun deleteTagByName(tagName: String): LiveData<Boolean>{
        val isDeleted: MutableLiveData<Boolean> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO){
            when (tagsRepository.deleteTags(tagName)) {
                HttpConstants.FAILED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.UNAUTHORISED -> {
                    isDeleted.postValue(false)
                }
                HttpConstants.NO_CONTENT_SUCCESS -> {
                    isDeleted.postValue(true)
                }
            }
        }
        return isDeleted
    }

    private suspend fun getSumDetails(tagName: String){
        val defaultCurrency = currencyRepository.defaultCurrency().currencyAttributes
        val currencyCode = defaultCurrency.code
        val withdrawalSum = transactionRepository.getTransactionSumByTagsAndTypeAndDateAndCurrency(
                tagName, "withdrawal",
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode
        )
        val depositSum = transactionRepository.getTransactionSumByTagsAndTypeAndDateAndCurrency(
                tagName, "deposit",
                DateTimeUtil.getStartOfMonth(),
                DateTimeUtil.getEndOfMonth(), currencyCode
        )
        val difference = depositSum.minus(withdrawalSum)
        transactionSum.postValue(
               arrayListOf(
                       DetailModel(getApplication<Application>().getString(R.string.withdrawal), defaultCurrency.symbol + withdrawalSum),
                       DetailModel(getApplication<Application>().getString(R.string.deposit), defaultCurrency.symbol + depositSum),
                       DetailModel("Total ", defaultCurrency.symbol + difference)
               )
        )
    }
}