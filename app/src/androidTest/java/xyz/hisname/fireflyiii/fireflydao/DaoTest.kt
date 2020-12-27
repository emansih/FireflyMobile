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
        runBlocking {
            globalScope.launch {
                fireflyDatabase.accountDataDao().insert(accountFactory)
                fireflyDatabase.accountDataDao().deleteAccountByType("asset")
                fireflyDatabase.accountDataDao().deleteAccountByType("revenue")
                fireflyDatabase.accountDataDao().deleteAccountByType("expenses")
                assertTrue("no asset",
                        fireflyDatabase.accountDataDao().getAccountsByType("asset").isEmpty())
                assertTrue("no revenue",
                        fireflyDatabase.accountDataDao().getAccountsByType("revenue").isEmpty())
                assertTrue("no expenses",
                        fireflyDatabase.accountDataDao().getAccountsByType("expenses").isEmpty())
            }
        }
    }

    @Test
    fun getAssetAccountData(){
        runBlocking {
            globalScope.launch {
                val accountFactory = DaoFactory.makeMultipleFakeAccount(20)
                accountFactory.forEach {
                    fireflyDatabase.accountDataDao().insert(it)
                }

                assertTrue(fireflyDatabase.accountDataDao().getAccountsByType("asset") ==
                        accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))

                assertTrue(fireflyDatabase.accountDataDao().getAccountsByType("revenue") ==
                        accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))

                assertTrue( fireflyDatabase.accountDataDao().getAccountsByType("expense") ==
                        accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))

            }
        }
    }

    @Test
    fun clearCurrencyTable(){
        runBlocking {
            globalScope.launch {
                val currencyFactory = DaoFactory.makeCounterfeitCurrency()
                fireflyDatabase.currencyDataDao().insert(currencyFactory)
                fireflyDatabase.currencyDataDao().deleteAllCurrency()
                fireflyDatabase.currencyDataDao().getPaginatedCurrency(1).collect { currency ->
                    assertTrue("table is empty", currency.isEmpty())
                }
            }
        }
    }

    @Test
    fun insertCurrencyData(){
        runBlocking {
            globalScope.launch {
                val currencyFactory = DaoFactory.makeCounterfeitCurrency()
                fireflyDatabase.currencyDataDao().insert(currencyFactory)
                fireflyDatabase.currencyDataDao().getPaginatedCurrency(1).collect { currency ->
                    assertTrue("table not empty" ,currency.isNotEmpty())
                }
            }
        }
    }

    @Test
    fun getCurrencyData() {
        runBlocking {
            globalScope.launch {
                val currencyFactory = DaoFactory.makeMultipleCounterfeitCurrency(5)
                currencyFactory.forEach {
                    fireflyDatabase.currencyDataDao().insert(it)
                }
                fireflyDatabase.currencyDataDao().getPaginatedCurrency(5).collect { retrievedCurrency ->
                    assertTrue(retrievedCurrency == currencyFactory.sortedWith(compareBy({ it.currencyId }, { it.currencyId })))
                }
            }
        }
    }

    @Test
    fun clearCategoryTable(){
        runBlocking {
            globalScope.launch {
                val categoryFactory = DaoFactory.makeCategory()
                fireflyDatabase.categoryDataDao().insert(categoryFactory)
                fireflyDatabase.categoryDataDao().deleteAllCategory()
                fireflyDatabase.categoryDataDao().getPaginatedCategory(1).collect { category ->
                    assertTrue("table is empty", category.isEmpty())
                }
            }
        }
    }

    @Test
    fun insertCategoryData(){
        runBlocking {
            globalScope.launch {
                val categoryFactory = DaoFactory.makeCategory()
                fireflyDatabase.categoryDataDao().insert(categoryFactory)
                fireflyDatabase.categoryDataDao().getPaginatedCategory(1).collect { category ->
                    assertTrue("table is empty", category.isEmpty())
                }
            }
        }
    }

    @Test
    fun getCategoryData() {
        runBlocking {
            globalScope.launch {
                val categoryFactory = DaoFactory.makeMultipleCategory(5)
                categoryFactory.forEach {
                    fireflyDatabase.categoryDataDao().insert(it)
                }
                fireflyDatabase.categoryDataDao().getPaginatedCategory(5).collect { retrievedCategory ->
                    assertTrue(retrievedCategory == categoryFactory.sortedWith(compareBy({ it.categoryId }, { it.categoryId })))
                }
            }
        }
    }

    @Test
    fun clearTagsTable(){
        runBlocking {
            globalScope.launch {
                val tagsFactory = DaoFactory.makeTags()
                fireflyDatabase.tagsDataDao().insert(tagsFactory)
                fireflyDatabase.tagsDataDao().deleteTags()
                assertTrue("table is empty", fireflyDatabase.tagsDataDao().getAllTags().isEmpty())
            }
        }
    }

    @Test
    fun insertTagsData(){
        runBlocking {
            globalScope.launch {
                val tagsFactory = DaoFactory.makeTags()
                fireflyDatabase.tagsDataDao().insert(tagsFactory)
                assertTrue("table not empty" , fireflyDatabase.tagsDataDao().getAllTags().isNotEmpty())
            }
        }
    }

    @Test
    fun getTagsData() {
        runBlocking {
            globalScope.launch {
                val tagsFactory = DaoFactory.makeMultipleTags(5)
                tagsFactory.forEach {
                    fireflyDatabase.tagsDataDao().insert(it)
                }
                val retrievedTags = fireflyDatabase.tagsDataDao().getAllTags()
                assertTrue(retrievedTags == tagsFactory.sortedWith(compareBy({ it.tagsId }, { it.tagsId })))
            }
        }
    }
}