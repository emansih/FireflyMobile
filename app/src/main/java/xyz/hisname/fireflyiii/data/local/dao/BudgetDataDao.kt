package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.budget.BudgetData

@Dao
abstract class BudgetDataDao: BaseDao<BudgetData> {

    @Query("SELECT * FROM budget")
    abstract fun getAllBudget(): LiveData<MutableList<BudgetData>>

}