package xyz.hisname.fireflyiii.ui.transaction.details

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.transaction_attachment_items.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.inflate
import java.io.File

class TransactionAttachmentRecyclerAdapter(private val items: MutableList<AttachmentData>,
                                           private val clickListener:(AttachmentData) -> Unit):
        DiffUtilAdapter<AttachmentData, TransactionAttachmentRecyclerAdapter.AttachmentAdapter>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentAdapter {
        context = parent.context
        return AttachmentAdapter(parent.inflate(R.layout.transaction_attachment_items))

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AttachmentAdapter, position: Int) = holder.bind(items[position], clickListener)

    inner class AttachmentAdapter(view: View): RecyclerView.ViewHolder(view) {
        fun bind(attachmentData: AttachmentData, clickListener: (AttachmentData) -> Unit){
            val fileName = attachmentData.attachmentAttributes.filename

            if(fileName.length >= 15){
                itemView.attachment_name.text = fileName.substring(0, 15) + "..."
            } else {
                itemView.attachment_name.text = fileName
            }

            if(File("${FileUtils().folderDirectory}/$fileName").exists()){
                Glide.with(context).load(IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_folder_open)
                        .sizeDp(12))
                        .into(itemView.downloadButton)
            } else {
                Glide.with(context).load(IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_file_download)
                        .sizeDp(12))
                        .into(itemView.downloadButton)
            }
            itemView.downloadButton.setOnClickListener { clickListener(attachmentData) }
        }
    }
}