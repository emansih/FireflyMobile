package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import xyz.hisname.fireflyiii.repository.models.category.CategoryData


@Dao
abstract class CategoryDataDao: BaseDao<CategoryData> {

    @Query("SELECT * FROM category")
    abstract fun getAllCategory(): MutableList<CategoryData>

    @Query("DELETE FROM category WHERE categoryId = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long): Int

    @Query("DELETE FROM category")
    abstract fun deleteAllCategory(): Int

    @Transaction
    @Query("SELECT * FROM category JOIN categoryFts ON (category.categoryId = " +
            "categoryFts.categoryId) WHERE categoryFts MATCH :categoryName")
    abstract fun searchCategory(categoryName: String): MutableList<CategoryData>
}