package xyz.hisname.fireflyiii.ui

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_markdown.*
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getCompatColor

class MarkdownFragment: BaseFragment() {

    private val toolbar by lazy {requireActivity().findViewById<Toolbar>(R.id.activity_toolbar) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_markdown, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidget()
        parseText()
    }

    private fun setWidget(){
        toolbar.visibility = View.GONE
        discardButton.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_close)
                        .color(getCompatColor(R.color.md_white_1000))
                        .sizeDp(12),null, null, null)
        doneButton.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_done)
                        .color(getCompatColor(R.color.md_white_1000))
                        .sizeDp(12),null, null, null)
        discardButton.setOnClickListener {
            handleBack()
        }
    }

    private fun parseText(){
        val extensions = arrayListOf(StrikethroughExtension.create(), AutolinkExtension.create())
        val parser = Parser.builder().extensions(extensions).build()
        val renderer = HtmlRenderer.builder().extensions(extensions).build()
        editableText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(editable: Editable) {

            }

            override fun beforeTextChanged(charsequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            @Suppress("DEPRECATION")
            override fun onTextChanged(charsequence: CharSequence, start: Int, before: Int, count: Int) {
                val document = parser.parse(charsequence.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    displayText.text = Html.fromHtml(renderer.render(document), Html.FROM_HTML_MODE_COMPACT)
                } else {
                    displayText.text = Html.fromHtml(renderer.render(document))
                }
            }

        })

    }

    override fun handleBack() {
        requireFragmentManager().popBackStack()
        toolbar.visibility = View.VISIBLE
    }


}