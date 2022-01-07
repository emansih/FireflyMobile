package xyz.hisname.fireflyiii.repository.models.search

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/* Supported endpoints:
 * /api/v1/autocomplete/categories
 * /api/v1/autocomplete/transactions
 * /api/v1/autocomplete/budgets
 * /api/v1/autocomplete/piggy-banks
 * /api/v1/autocomplete/currencies
 * /api/v1/autocomplete/accounts
 * /api/v1/autocomplete/tags
 * /api/v1/autocomplete/bills
 */
@JsonClass(generateAdapter = true)
data class SearchModelItem(
    val id: Long,
    val name: String,
    val type: String?,
    @Json(name = "currency_symbol")
    val currencySymbol: String?
)