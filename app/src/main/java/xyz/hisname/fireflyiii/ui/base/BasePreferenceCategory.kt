package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder

class BasePreferenceCategory@JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null): PreferenceCategory(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val summary = holder.findViewById(android.R.id.summary) as TextView
        summary.isSingleLine = false
        summary.maxLines = 7
    }
}