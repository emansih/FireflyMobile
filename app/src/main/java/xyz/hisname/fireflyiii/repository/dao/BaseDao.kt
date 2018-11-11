package xyz.hisname.fireflyiii.repository.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy

interface BaseDao<T>{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg obj: T)
}