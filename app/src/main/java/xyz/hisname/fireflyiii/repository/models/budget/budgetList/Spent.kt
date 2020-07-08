package xyz.hisname.fireflyiii.repository.models.budget.budgetList

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "spentList"/*, foreignKeys = [ForeignKey(entity = BudgetListData::class,
        parentColumns = arrayOf("budgetListId"), childColumns = arrayOf("spentFK"), onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE)]*/)
data class Spent(
// For some reason, foreign keys does not work. will have to settle with a hack :(
        @PrimaryKey(autoGenerate = false)
        var spentId: Long,
        @SerializedName("sum")
        var amount: Double,
        var currency_code: String,
        var currency_decimal_places: Int,
        var currency_id: Int,
        var currency_symbol: String
)