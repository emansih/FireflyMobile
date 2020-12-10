package xyz.hisname.fireflyiii.repository.models.accounts

import androidx.room.Embedded
import androidx.room.Entity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity
data class AccountsModel(
        @Embedded
        val data: List<AccountData>,
        val meta: Meta
)