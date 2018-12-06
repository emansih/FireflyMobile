package xyz.hisname.fireflyiii.ui.base

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.google.android.material.R


class TextInputAutoCompleteTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int =  R.attr.editTextStyle
) : AppCompatAutoCompleteTextView(context,attrs, defStyleAttr)
