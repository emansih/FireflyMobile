package xyz.hisname.fireflyiii.repository.currency

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

class CurrencyRepository(private val currencyDao: CurrencyDataDao) {

    val allCurrency = currencyDao.getAllCurrency()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertCurrency(currency: CurrencyData){
        currencyDao.insert(currency)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getCurrencyByCode(currencyCode: String): MutableList<CurrencyData>{
        return currencyDao.getCurrencyByCode(currencyCode)
    }
}