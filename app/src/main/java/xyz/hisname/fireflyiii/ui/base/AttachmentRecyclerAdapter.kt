package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.transaction_attachment_items.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.util.extension.inflate
import java.io.File

class AttachmentRecyclerAdapter(private val items: MutableList<AttachmentData>,
                                private val shouldShowDownload: Boolean = true,
                                private val clickListener:(AttachmentData) -> Unit,
                                private val removeItemListener:(position: Int) -> Unit):
        RecyclerView.Adapter<AttachmentRecyclerAdapter.AttachmentAdapter>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentAdapter {
        context = parent.context
        return AttachmentAdapter(parent.inflate(R.layout.transaction_attachment_items))

    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AttachmentAdapter, position: Int) = holder.bind(items[position], clickListener, position)

    inner class AttachmentAdapter(view: View): RecyclerView.ViewHolder(view) {
        fun bind(attachmentData: AttachmentData, clickListener: (AttachmentData) -> Unit, removeItemListener: Int){
            val fileName = attachmentData.attachmentAttributes.filename
            itemView.attachment_name.text = fileName
            if(shouldShowDownload) {
                val downloadedFile = File(context.getExternalFilesDir(null).toString() + File.separator + fileName)
                if (downloadedFile.exists()) {
                    Glide.with(context).load(IconicsDrawable(context).apply {
                        icon = GoogleMaterial.Icon.gmd_folder_open
                        colorRes = R.color.md_yellow_700
                        sizeDp = 12
                    }).into(itemView.downloadButton)
                } else {
                    Glide.with(context).load(IconicsDrawable(context).apply {
                        icon = GoogleMaterial.Icon.gmd_file_download
                        colorRes = R.color.md_green_500
                        sizeDp = 12
                    }).into(itemView.downloadButton)
                }
            } else {
                itemView.downloadButton.setImageDrawable(IconicsDrawable(context, GoogleMaterial.Icon.gmd_close).apply {
                    colorRes = R.color.md_red_500
                    sizeDp = 12
                })
                itemView.downloadButton.setOnClickListener { removeItemListener(removeItemListener) }
            }
            itemView.downloadButton.setOnClickListener { clickListener(attachmentData) }
            itemView.setOnClickListener { clickListener(attachmentData) }
        }
    }
}