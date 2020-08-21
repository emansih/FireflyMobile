package xyz.hisname.fireflyiii.fireflydao

import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase

@RunWith(AndroidJUnit4ClassRunner ::class)
open class DaoTest {

    private lateinit var fireflyDatabase: AppDatabase
    private lateinit var globalScope: GlobalScope

    @Before
    fun initDb(){
        globalScope = GlobalScope
        fireflyDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().context,
                AppDatabase::class.java).build()
    }

    @After
    fun closeDb(){
        fireflyDatabase.close()
    }

    @Test
    fun clearAccountTable(){
        val accountFactory = DaoFactory.makeAccount()
        fireflyDatabase.accountDataDao().insert(accountFactory)
        fireflyDatabase.accountDataDao().deleteAccountByType("asset")
        fireflyDatabase.accountDataDao().deleteAccountByType("revenue")
        fireflyDatabase.accountDataDao().deleteAccountByType("expenses")
        runBlocking {
            globalScope.launch {
                fireflyDatabase.accountDataDao().getAccountByType("asset").collect { asset ->
                    assertTrue("no asset", asset.isEmpty())
                }
                fireflyDatabase.accountDataDao().getAccountByType("revenue").collect { revenue ->
                    assertTrue("no revenue", revenue.isEmpty())
                }
                fireflyDatabase.accountDataDao().getAccountByType("expenses").collect{ expenses ->
                    assertTrue("no expenses", expenses.isEmpty())
                }
            }
        }
    }

    @Test
    fun retrieveAssetAccountData(){
        val accountFactory = DaoFactory.makeMultipleFakeAccount(20)
        accountFactory.forEach {
            fireflyDatabase.accountDataDao().insert(it)
        }
        runBlocking {
            globalScope.launch {
                fireflyDatabase.accountDataDao().getAccountByType("asset").collect { assetAccount ->
                    assertTrue(assetAccount == accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))
                }
                fireflyDatabase.accountDataDao().getAccountByType("revenue").collect { revenueAccount ->
                    assertTrue(revenueAccount == accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))
                }
                fireflyDatabase.accountDataDao().getAccountByType("expense").collect { expenseAccount ->
                    assertTrue(expenseAccount == accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))
                }
            }
        }
    }

    @Test
    fun clearCurrencyTable(){
        val currencyFactory = DaoFactory.makeCounterfeitCurrency()
        fireflyDatabase.currencyDataDao().insert(currencyFactory)
        fireflyDatabase.currencyDataDao().deleteAllCurrency()
        runBlocking {
            globalScope.launch {
                fireflyDatabase.currencyDataDao().getPaginatedCurrency(1).collect { currency ->
                    assertTrue("table is empty", currency.isEmpty())
                }
            }
        }
    }

    @Test
    fun insertCurrencyData(){
        val currencyFactory = DaoFactory.makeCounterfeitCurrency()
        fireflyDatabase.currencyDataDao().insert(currencyFactory)
        runBlocking {
            globalScope.launch {
                fireflyDatabase.currencyDataDao().getPaginatedCurrency(1).collect { currency ->
                    assertTrue("table not empty" ,currency.isNotEmpty())
                }
            }
        }
    }

    @Test
    fun retrieveCurrencyData() {
        val currencyFactory = DaoFactory.makeMultipleCounterfeitCurrency(5)
        currencyFactory.forEach {
            fireflyDatabase.currencyDataDao().insert(it)
        }
        runBlocking {
            globalScope.launch {
                fireflyDatabase.currencyDataDao().getPaginatedCurrency(5).collect { retrievedCurrency ->
                    assertTrue(retrievedCurrency == currencyFactory.sortedWith(compareBy({ it.currencyId }, { it.currencyId })))
                }
            }
        }
    }

    @Test
    fun clearCategoryTable(){
        val categoryFactory = DaoFactory.makeCategory()
        fireflyDatabase.categoryDataDao().insert(categoryFactory)
        fireflyDatabase.categoryDataDao().deleteAllCategory()
        runBlocking {
            globalScope.launch {
                fireflyDatabase.categoryDataDao().getPaginatedCategory(1).collect { category ->
                    assertTrue("table is empty", category.isEmpty())
                }
            }
        }
    }

    @Test
    fun insertCategoryData(){
        val categoryFactory = DaoFactory.makeCategory()
        fireflyDatabase.categoryDataDao().insert(categoryFactory)
        runBlocking {
            globalScope.launch {
                fireflyDatabase.categoryDataDao().getPaginatedCategory(1).collect { category ->
                    assertTrue("table is empty", category.isEmpty())
                }
            }
        }
    }

    @Test
    fun retrieveCategoryData() {
        val categoryFactory = DaoFactory.makeMultipleCategory(5)
        categoryFactory.forEach {
            fireflyDatabase.categoryDataDao().insert(it)
        }
        runBlocking {
            globalScope.launch {
                fireflyDatabase.categoryDataDao().getPaginatedCategory(5).collect { retrievedCategory ->
                    assertTrue(retrievedCategory == categoryFactory.sortedWith(compareBy({ it.categoryId }, { it.categoryId })))
                }
            }
        }
    }

    @Test
    fun clearTagsTable(){
        val tagsFactory = DaoFactory.makeTags()
        fireflyDatabase.tagsDataDao().insert(tagsFactory)
        fireflyDatabase.tagsDataDao().deleteTags()
        assertTrue("table is empty", fireflyDatabase.tagsDataDao().getAllTags().isEmpty())
    }

    @Test
    fun insertTagsData(){
        val tagsFactory = DaoFactory.makeTags()
        fireflyDatabase.tagsDataDao().insert(tagsFactory)
        assertTrue("table not empty" , fireflyDatabase.tagsDataDao().getAllTags().isNotEmpty())
    }

    @Test
    fun retrieveTagsData() {
        val tagsFactory = DaoFactory.makeMultipleTags(5)
        tagsFactory.forEach {
            fireflyDatabase.tagsDataDao().insert(it)
        }
        val retrievedTags = fireflyDatabase.tagsDataDao().getAllTags()
        assertTrue(retrievedTags == tagsFactory.sortedWith(compareBy({ it.tagsId }, { it.tagsId })))
    }
}