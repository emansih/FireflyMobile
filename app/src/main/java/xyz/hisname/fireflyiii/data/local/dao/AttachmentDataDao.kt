package xyz.hisname.fireflyiii.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData

@Dao
abstract class AttachmentDataDao: BaseDao<AttachmentData> {

    @Query("SELECT * FROM attachment_info WHERE attachable_id =:journalId")
    abstract fun getAttachmentFromJournalId(journalId: Long): List<AttachmentData>

}