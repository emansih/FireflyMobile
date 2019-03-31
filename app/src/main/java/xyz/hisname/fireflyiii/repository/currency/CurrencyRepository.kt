package xyz.hisname.fireflyiii.repository.currency

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.CurrencyDataDao
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData

class CurrencyRepository(private val currencyDao: CurrencyDataDao) {

    val allCurrency = currencyDao.getAllCurrency()
    val enabledCurrency = currencyDao.getEnabledCurrencyByCode()

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

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getCurrencyById(currencyId: Long) = currencyDao.getCurrencyById(currencyId)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteDefaultCurrency() = currencyDao.deleteDefaultCurrency()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun defaultCurrency() = currencyDao.getDefaultCurrency()
 }