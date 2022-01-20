package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.FireflyUsers

@Dao
abstract class FireflyUserDao: BaseDao<FireflyUsers> {

    @Query("SELECT * FROM firefly_users ORDER BY activeUser DESC")
    abstract suspend fun getAllUser(): List<FireflyUsers>

    @Query("SELECT userEmail FROM firefly_users WHERE activeUser =:isActive")
    abstract suspend fun getCurrentActiveUserEmail(isActive: Boolean = true): String

    @Query("SELECT userHost FROM firefly_users WHERE activeUser =:isActive")
    abstract suspend fun getCurrentActiveUserUrl(isActive: Boolean = true): String

    @Query("SELECT uniqueHash FROM firefly_users WHERE activeUser =:isActive")
    abstract suspend fun getUniqueHash(isActive: Boolean = true): String

    @Query("UPDATE firefly_users SET activeUser =:activeUser WHERE id =:userId")
    abstract suspend fun updateActiveUser(userId: Long, activeUser: Boolean = true)

    // Questionable piece of code....
    @Query("UPDATE firefly_users SET activeUser =:activeUser WHERE activeUser =:aactiveUser")
    abstract suspend fun removeActiveUser(activeUser: Boolean = false, aactiveUser: Boolean = true)

    @Query("UPDATE firefly_users SET activeUser =:activeUser WHERE uniqueHash =:uniqueHash")
    abstract suspend fun updateActiveUser(uniqueHash: String, activeUser: Boolean)

    @Query("UPDATE firefly_users SET userEmail =:email WHERE activeUser =:activeUser AND uniqueHash =:uniqueHash")
    abstract suspend fun updateActiveUserEmail(uniqueHash: String, email: String, activeUser: Boolean = true)

    @Query("UPDATE firefly_users SET userHost =:userUrl WHERE activeUser =:activeUser AND uniqueHash =:uniqueHash")
    abstract suspend fun updateActiveUserHost(uniqueHash: String, userUrl: String, activeUser: Boolean = true)

    @Query("DELETE FROM firefly_users WHERE activeUser =:isActive")
    abstract suspend fun deleteCurrentUser(isActive: Boolean = true)

    @Query("DELETE FROM firefly_users WHERE id =:primaryKey")
    abstract suspend fun deleteUserByPrimaryKey(primaryKey: Long)

    @Query("SELECT * FROM firefly_users WHERE uniqueHash=:uniqueHash")
    abstract suspend fun getUserByHash(uniqueHash: String): FireflyUsers
}