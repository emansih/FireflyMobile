package xyz.hisname.fireflyiii.fireflydao

import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.hisname.fireflyiii.data.local.dao.AppDatabase

@RunWith(AndroidJUnit4ClassRunner ::class)
open class DaoTest {

    private lateinit var fireflyDatabase: AppDatabase

    @Before
    fun initDb(){
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
        assertTrue("no asset", fireflyDatabase.accountDataDao().getAccountByType("asset").isEmpty())
        fireflyDatabase.accountDataDao().deleteAccountByType("revenue")
        assertTrue("no revenue", fireflyDatabase.accountDataDao().getAccountByType("revenue").isEmpty())
        fireflyDatabase.accountDataDao().deleteAccountByType("expenses")
        assertTrue("no expenses", fireflyDatabase.accountDataDao().getAccountByType("expenses").isEmpty())
    }

    @Test
    fun retrieveAssetAccountData(){
        val accountFactory = DaoFactory.makeMultipleFakeAccount(20)
        accountFactory.forEach {
            fireflyDatabase.accountDataDao().insert(it)
        }
        val assetAccount = fireflyDatabase.accountDataDao().getAccountByType("asset")
        val revenueAccount = fireflyDatabase.accountDataDao().getAccountByType("revenue")
        val expenseAccount = fireflyDatabase.accountDataDao().getAccountByType("expense")
        assertTrue(assetAccount == accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))
        assertTrue(revenueAccount == accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))
        assertTrue(expenseAccount == accountFactory.sortedWith(compareBy({ it.accountId }, { it.accountId })))
    }

    @Test
    fun clearCurrencyTable(){
        val currencyFactory = DaoFactory.makeCounterfeitCurrency()
        fireflyDatabase.currencyDataDao().insert(currencyFactory)
        fireflyDatabase.currencyDataDao().deleteAllCurrency()
        assertTrue("table is empty", fireflyDatabase.currencyDataDao().getPaginatedCurrency(1).isEmpty())
    }

    @Test
    fun insertCurrencyData(){
        val currencyFactory = DaoFactory.makeCounterfeitCurrency()
        fireflyDatabase.currencyDataDao().insert(currencyFactory)
        assertTrue("table not empty" , fireflyDatabase.currencyDataDao().getPaginatedCurrency(1).isNotEmpty())
    }

    @Test
    fun retrieveCurrencyData() {
        val currencyFactory = DaoFactory.makeMultipleCounterfeitCurrency(5)
        currencyFactory.forEach {
            fireflyDatabase.currencyDataDao().insert(it)
        }
        val retrievedCurrency = fireflyDatabase.currencyDataDao().getPaginatedCurrency(5)
        assertTrue(retrievedCurrency == currencyFactory.sortedWith(compareBy({ it.currencyId }, { it.currencyId })))
    }

    @Test
    fun clearCategoryTable(){
        val categoryFactory = DaoFactory.makeCategory()
        fireflyDatabase.categoryDataDao().insert(categoryFactory)
        fireflyDatabase.categoryDataDao().deleteAllCategory()
        assertTrue("table is empty", fireflyDatabase.categoryDataDao().getPaginatedCategory(1).isEmpty())
    }

    @Test
    fun insertCategoryData(){
        val categoryFactory = DaoFactory.makeCategory()
        fireflyDatabase.categoryDataDao().insert(categoryFactory)
        assertTrue("table not empty" , fireflyDatabase.categoryDataDao().getPaginatedCategory(1).isNotEmpty())
    }

    @Test
    fun retrieveCategoryData() {
        val categoryFactory = DaoFactory.makeMultipleCategory(5)
        categoryFactory.forEach {
            fireflyDatabase.categoryDataDao().insert(it)
        }
        val retrievedCategory = fireflyDatabase.categoryDataDao().getPaginatedCategory(5)
        assertTrue(retrievedCategory == categoryFactory.sortedWith(compareBy({ it.categoryId }, { it.categoryId })))
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
        val retrievedCurrency = fireflyDatabase.tagsDataDao().getAllTags()
        assertTrue(retrievedCurrency == tagsFactory.sortedWith(compareBy({ it.tagsId }, { it.tagsId })))
    }
}