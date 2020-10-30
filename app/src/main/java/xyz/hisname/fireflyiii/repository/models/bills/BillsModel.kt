package xyz.hisname.fireflyiii.repository.models.bills

import androidx.room.Embedded
import androidx.room.Entity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity
data class BillsModel(
        @Embedded
        val data: MutableCollection<BillData>,
        val meta: Meta)

