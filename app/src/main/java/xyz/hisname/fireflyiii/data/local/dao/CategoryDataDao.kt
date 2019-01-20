package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.category.CategoryData


@Dao
abstract class CategoryDataDao: BaseDao<CategoryData> {

    @Query("SELECT * FROM category")
    abstract fun getAllCategory(): MutableList<CategoryData>

    @Query("DELETE FROM category WHERE categoryId = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long): Int

    @Query("DELETE FROM category")
    abstract fun deleteAllCategory(): Int

    @Query("SELECT * FROM category WHERE name LIKE :categoryName")
    abstract fun searchCategory(categoryName: String): MutableList<CategoryData>

}