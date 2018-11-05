package xyz.hisname.fireflyiii.repository.models.error

import com.google.gson.annotations.SerializedName

data class Errors(
        val name: List<String>?,
        val account_id: List<String>?,
        val current_amount: List<String>?,
        val targetDate: List<String>?,
        val currency_code: List<String>?,
        val amount_min: List<String>?,
        val repeat_freq: List<String>?,
        val automatch: List<String>?,
        @SerializedName("transactions.0.destination_name")
        val transactions_destination_name: List<String>?,
        @SerializedName("transactions.0.destination_id")
        val transaction_destination_id: List<String>?,
        @SerializedName("transactions.0.currency_code")
        val transactions_currency: List<String>?,
        @SerializedName("transactions.0.source_name")
        val transactions_source_name: List<String>?,
        val bill_name: List<String>?,
        val piggy_bank_name: List<String>?
)