package xyz.hisname.fireflyiii.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.category.CategoryData


@Dao
abstract class CategoryDataDao: BaseDao<CategoryData> {

    @Query("SELECT * FROM category")
    abstract fun getAllCategory(): LiveData<MutableList<CategoryData>>

    @Query("SELECT * FROM category")
    abstract fun getCategories(): MutableList<CategoryData>


    @Query("DELETE FROM category WHERE categoryId = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long): Int

}