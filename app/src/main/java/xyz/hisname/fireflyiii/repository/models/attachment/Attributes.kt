/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.repository.models.attachment

import android.net.Uri
import androidx.room.Entity
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class Attributes(
        val attachable_id: Int,
        val attachable_type: String,
        val created_at: String,
        val download_uri: Uri,
        val filename: String,
        val md5: String,
        val mime: String,
        val notes: String,
        val size: Int,
        val title: String,
        val updated_at: String,
        val upload_uri: String
)