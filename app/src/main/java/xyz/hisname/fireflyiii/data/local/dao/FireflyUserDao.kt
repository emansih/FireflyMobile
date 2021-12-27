package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.FireflyUsers

@Dao
abstract class FireflyUserDao: BaseDao<FireflyUsers> {

    @Query("SELECT * FROM firefly_users")
    abstract fun getAllUser(): List<FireflyUsers>

    @Query("SELECT userEmail FROM firefly_users WHERE activeUser =:isActive")
    abstract fun getCurrentActiveUserEmail(isActive: Boolean = true): String

    @Query("SELECT userHost FROM firefly_users WHERE activeUser =:isActive")
    abstract fun getCurrentActiveUserUrl(isActive: Boolean = true): String

    @Query("SELECT uniqueHash FROM firefly_users WHERE activeUser =:isActive")
    abstract fun getUniqueHash(isActive: Boolean = true): String

    @Query("UPDATE firefly_users SET activeUser =:activeUser WHERE userEmail =:userName AND userHost =:userUrl")
    abstract fun updateActiveUser(userName: String, userUrl: String, activeUser: Boolean = true)

    @Query("UPDATE firefly_users SET activeUser =:activeUser WHERE uniqueHash =:uniqueHash")
    abstract fun updateActiveUser(uniqueHash: String, activeUser: Boolean)

    @Query("UPDATE firefly_users SET userEmail =:email WHERE activeUser =:activeUser AND uniqueHash =:uniqueHash")
    abstract fun updateActiveUserEmail(uniqueHash: String, email: String, activeUser: Boolean = true)

    @Query("UPDATE firefly_users SET userHost =:userUrl WHERE activeUser =:activeUser AND uniqueHash =:uniqueHash")
    abstract fun updateActiveUserHost(uniqueHash: String, userUrl: String, activeUser: Boolean = true)

    @Query("UPDATE firefly_users SET activeUser =:activeUser")
    abstract fun unsetDefaultUser(activeUser: Boolean = false)

    @Query("DELETE FROM firefly_users WHERE activeUser =:isActive")
    abstract fun deleteCurrentUser(isActive: Boolean = true)

    @Query("DELETE FROM firefly_users WHERE id =:primaryKey")
    abstract fun deleteUserByPrimaryKey(primaryKey: Long)

    @Query("SELECT * FROM firefly_users WHERE uniqueHash=:uniqueHash")
    abstract fun getUserByHash(uniqueHash: String): FireflyUsers
}