package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.category.CategoryData


@Dao
abstract class CategoryDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addCategory(vararg categoryData: CategoryData)

    @Query("SELECT * FROM category")
    abstract fun getAllCategory(): LiveData<MutableList<CategoryData>>

}