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

package xyz.hisname.fireflyiii.ui.bills

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase
import xyz.hisname.fireflyiii.data.remote.firefly.api.BillsService
import xyz.hisname.fireflyiii.data.remote.firefly.api.CurrencyService
import xyz.hisname.fireflyiii.repository.BaseViewModel
import xyz.hisname.fireflyiii.repository.bills.BillRepository
import xyz.hisname.fireflyiii.repository.bills.BillsPaidRepository
import xyz.hisname.fireflyiii.repository.currency.CurrencyRepository
import xyz.hisname.fireflyiii.repository.models.bills.BillsStatusModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import java.math.BigDecimal

class BillsBottomSheetViewModel(application: Application): BaseViewModel(application) {

    private val billService = genericService().create(BillsService::class.java)
    private val billDataDao = AppDatabase.getInstance(application, getUniqueHash()).billDataDao()
    private val billPaidDao = AppDatabase.getInstance(application, getUniqueHash()).billPaidDao()
    private val billPayDao = AppDatabase.getInstance(application, getUniqueHash()).billPayDao()
    private val billRepository = BillRepository(billDataDao, billService)
    private val billPaidRepository = BillsPaidRepository(billPaidDao, billService)

    private val currencyRepository = CurrencyRepository(
            AppDatabase.getInstance(application, getUniqueHash()).currencyDataDao(),
            genericService().create(CurrencyService::class.java))
    val billPayableToday = MutableLiveData<String>()

    fun getBills(): LiveData<List<BillsStatusModel>>{
        val billList = MutableLiveData<List<BillsStatusModel>>()
        viewModelScope.launch(Dispatchers.IO){
            val defaultSymbol = currencyRepository.defaultCurrency().currencyAttributes.symbol
            var totalPayableTodayInDefault = 0.toBigDecimal()
            val billDue = billRepository.getBillDueFromDate(DateTimeUtil.getTodayDate())
            val billPaidId = billPaidRepository.getBillPaidByDate(DateTimeUtil.getTodayDate(),
                    DateTimeUtil.getTodayDate(), billPayDao)
            val billStatusModel = arrayListOf<BillsStatusModel>()
            billDue.forEach { billData ->
                val amountToDisplay = billData.billAttributes.amount_max
                        .plus(billData.billAttributes.amount_min)
                        .div(BigDecimal.valueOf(2))
                if(defaultSymbol.contentEquals(billData.billAttributes.currency_symbol)){
                    totalPayableTodayInDefault += amountToDisplay
                }
                billStatusModel.add(
                        BillsStatusModel(
                                billData.billId,
                                billData.billAttributes.name,
                                amountToDisplay,
                                billData.billAttributes.currency_symbol,
                                billPaidId.contains(billData.billId)
                        )
                )
            }
            billPayableToday.postValue("~" + defaultSymbol + "" + totalPayableTodayInDefault)
            billList.postValue(billStatusModel)
        }
        return billList
    }
}