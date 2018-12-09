package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy

interface BaseDao<T>{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg obj: T)
}