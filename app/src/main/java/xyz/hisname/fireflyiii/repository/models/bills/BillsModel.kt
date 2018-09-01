package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class BillsModel(
        @Embedded
        val data: MutableCollection<BillData>,
        val meta: Meta)

