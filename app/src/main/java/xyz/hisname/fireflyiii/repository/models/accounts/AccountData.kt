package xyz.hisname.fireflyiii.repository.models.accounts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "accounts")
data class AccountData(
        @Ignore
        var type: String = "",
        @PrimaryKey(autoGenerate = false)
        @SerializedName("id")
        var accountId: Long? = null,
        @Embedded
        @SerializedName("attributes")
        var accountAttributes: AccountAttributes? = null,
        @Ignore
        var links: Links? = null
)