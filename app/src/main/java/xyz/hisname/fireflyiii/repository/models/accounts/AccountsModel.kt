package xyz.hisname.fireflyiii.repository.models.accounts

import androidx.room.Embedded

data class AccountsModel(
        @Embedded
        val data: MutableCollection<AccountData>,
        val meta: Meta
)