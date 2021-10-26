package xyz.hisname.fireflyiii.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "firefly_users")
data class FireflyUsers(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val uniqueHash: String,
    val userEmail: String,
    val userHost: String,
    val activeUser: Boolean
)