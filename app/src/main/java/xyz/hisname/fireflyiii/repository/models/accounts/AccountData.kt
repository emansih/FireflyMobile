package xyz.hisname.fireflyiii.repository.models.accounts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "accounts")
data class AccountData(
        @PrimaryKey(autoGenerate = false)
        @Json(name ="id")
        val accountId: Long = 0,
        @Embedded
        @Json(name ="attributes")
        val accountAttributes: AccountAttributes
)