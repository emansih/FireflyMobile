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

package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.TransactionAttachmentItemsBinding
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import java.io.File

class AttachmentRecyclerAdapter(private val items: MutableList<AttachmentData>,
                                private val shouldShowDownload: Boolean = true,
                                private val clickListener:(AttachmentData) -> Unit,
                                private val removeItemListener:(position: Int) -> Unit):
        RecyclerView.Adapter<AttachmentRecyclerAdapter.AttachmentAdapter>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentAdapter {
        context = parent.context
        val itemView = TransactionAttachmentItemsBinding.inflate(LayoutInflater.from(context), parent, false)
        return AttachmentAdapter(itemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AttachmentAdapter, position: Int) = holder.bind(items[position], clickListener, position)

    inner class AttachmentAdapter(
        private val view: TransactionAttachmentItemsBinding
    ): RecyclerView.ViewHolder(view.root) {
        fun bind(attachmentData: AttachmentData, clickListener: (AttachmentData) -> Unit, removeItemListener: Int){
            val fileName = attachmentData.attachmentAttributes.filename
            view.attachmentName.text = fileName
            if(shouldShowDownload) {
                val downloadedFile = File(context.getExternalFilesDir(null).toString() + File.separator + fileName)
                if (downloadedFile.exists()) {
                    Glide.with(context).load(IconicsDrawable(context).apply {
                        icon = GoogleMaterial.Icon.gmd_folder_open
                        colorRes = R.color.md_yellow_700
                        sizeDp = 12
                    }).into(view.downloadButton)
                } else {
                    Glide.with(context).load(IconicsDrawable(context).apply {
                        icon = GoogleMaterial.Icon.gmd_file_download
                        colorRes = R.color.md_green_500
                        sizeDp = 12
                    }).into(view.downloadButton)
                }
            } else {
                view.downloadButton.setImageDrawable(IconicsDrawable(context, GoogleMaterial.Icon.gmd_close).apply {
                    colorRes = R.color.md_red_500
                    sizeDp = 12
                })
                view.downloadButton.setOnClickListener { removeItemListener(removeItemListener) }
            }
            view.downloadButton.setOnClickListener { clickListener(attachmentData) }
            itemView.setOnClickListener { clickListener(attachmentData) }
        }
    }
}