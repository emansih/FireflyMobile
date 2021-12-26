package xyz.hisname.fireflyiii.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "firefly_users", indices = [Index(value = ["uniqueHash"], unique = true)])
data class FireflyUsers(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val uniqueHash: String,
    val userEmail: String,
    val userHost: String,
    val activeUser: Boolean
)