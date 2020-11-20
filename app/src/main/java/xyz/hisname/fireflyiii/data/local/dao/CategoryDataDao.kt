package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.hisname.fireflyiii.repository.models.category.CategoryData


@Dao
abstract class CategoryDataDao: BaseDao<CategoryData> {

    @Query("DELETE FROM category WHERE categoryId = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long): Int

    @Query("DELETE FROM category")
    abstract fun deleteAllCategory(): Int

    @Query("SELECT * FROM category order by categoryId desc limit :limitNumber")
    abstract fun getPaginatedCategory(limitNumber: Int): Flow<MutableList<CategoryData>>

    @Query("SELECT * FROM category JOIN categoryFts ON category.categoryId == categoryFts.categoryId WHERE categoryFts MATCH :categoryName GROUP BY categoryFts.categoryId")
    abstract fun searchCategory(categoryName: String): MutableList<CategoryData>

    @Query("SELECT * FROM category WHERE categoryId =:categoryId")
    abstract fun getCategoryById(categoryId: Long): CategoryData

}