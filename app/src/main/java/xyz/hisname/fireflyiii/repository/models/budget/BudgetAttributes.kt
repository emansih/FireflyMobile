package xyz.hisname.fireflyiii.repository.models.budget

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BudgetAttributes(
        @PrimaryKey(autoGenerate = true)
        val budgetIdPlaceHolder: Long,
        val created_at: String,
        val updated_at: String,
        val currency_id: Int,
        val currency_code: String,
        val currency_symbol: String,
        val currency_decimal_places: Int,
        val amount: BigDecimal,
        @SerializedName("start")
        val start_date: String,
        @SerializedName("end")
        val end_date: String
)