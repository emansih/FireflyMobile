package xyz.hisname.fireflyiii.repository

import xyz.hisname.fireflyiii.data.local.dao.FireflyUserDao

class UserRepository(private val fireflyUserDao: FireflyUserDao) {

    suspend fun getDefaultUserUniqueHash(): String{
        return fireflyUserDao.getUniqueHash()
    }

    suspend fun getDefaultUserUrl(): String {
        return fireflyUserDao.getCurrentActiveUserUrl()
    }

    suspend fun getDefaultUserEmail(): String {
        return fireflyUserDao.getCurrentActiveUserEmail()
    }
}