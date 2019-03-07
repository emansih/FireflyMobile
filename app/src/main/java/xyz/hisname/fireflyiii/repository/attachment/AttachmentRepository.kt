package xyz.hisname.fireflyiii.repository.attachment

import androidx.annotation.WorkerThread
import xyz.hisname.fireflyiii.data.local.dao.AttachmentDataDao
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData

class AttachmentRepository(private val attachmentDao: AttachmentDataDao) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAttachmentInfo(attachment: AttachmentData) = attachmentDao.insert(attachment)


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAttachmentFromJournalId(journalId: Long) = attachmentDao.getAttachmentFromJournalId(journalId)
}