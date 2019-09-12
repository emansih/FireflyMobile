package xyz.hisname.fireflyiii.repository.models.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

@Entity(tableName = "transactionTable")
data class Transactions(
        @PrimaryKey(autoGenerate = false)
        var transaction_journal_id: Long,
        var amount: Double,
        var bill_id: Long,
        var bill_name: String?,
        var book_date: String?,
        var budget_id: Long,
        var budget_name: String?,
        var bunq_payment_id: Long,
        var category_id: Long,
        var category_name: String,
        var currency_code: String,
        var currency_decimal_places: Int,
        var currency_id: Long,
        var currency_name: String,
        var currency_symbol: String,
        var date: LocalDateTime,
        var description: String?,
        var destination_iban: String?,
        var destination_id: Long,
        var destination_name: String,
        var destination_type: String,
        var due_date: String?,
        var external_id: Long,
        var foreign_amount: Double,
        var foreign_currency_code: String?,
        var foreign_currency_decimal_places: String?,
        var foreign_currency_id: Long,
        var foreign_currency_symbol: String?,
        var import_hash_v2: String?,
        var interest_date: String?,
        var internal_reference: String?,
        var invoice_date: String?,
        var notes: String?,
        var order: Int,
        var original_source: String?,
        var payment_date: String?,
        var process_date: String?,
        var reconciled: Boolean?,
        var recurrence_id: Long?,
        var sepa_batch_id: Long?,
        var sepa_cc: String?,
        var sepa_ci: String?,
        var sepa_country: String?,
        var sepa_ct_id: Long?,
        var sepa_ct_op: String?,
        var sepa_db: String?,
        var sepa_ep: String?,
        var source_iban: String?,
        var source_id: Long?,
        var source_name: String?,
        var source_type: String?,
        var tags: List<String>,
        @SerializedName("type")
        var transactionType: String?,
        var user: Int
)