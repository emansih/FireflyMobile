package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData

@Dao
abstract class AccountsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addAccounts(vararg accountData: AccountData)

    @Query("SELECT * FROM accounts")
    abstract fun getAllAccounts(): LiveData<MutableList<AccountData>>


}