package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

@Entity(tableName = "transactionTable")
data class Transactions(
        @PrimaryKey(autoGenerate = false)
        var transaction_journal_id: Long,
        var amount: Double,
        var budget_id: Long,
        var budget_name: String?,
        var category_id: Long,
        var category_name: String?,
        var currency_code: String,
        var currency_decimal_places: Int,
        var currency_id: Long,
        var currency_name: String,
        var currency_symbol: String,
        var date: OffsetDateTime,
        var description: String,
        var destination_id: Long,
        var destination_name: String,
        var destination_type: String,
        var due_date: String?,
        var foreign_amount: Double?,
        var foreign_currency_code: String?,
        var foreign_currency_decimal_places: String?,
        var foreign_currency_id: Long,
        var foreign_currency_symbol: String?,
        var notes: String?,
        var order: Int,
        var source_iban: String?,
        var source_id: Long?,
        var source_name: String?,
        var source_type: String?,
        var tags: List<String>,
        @SerializedName("type")
        var transactionType: String?,
        var user: Int,
        var piggy_bank_name: String?,
        var isPending: Boolean
)