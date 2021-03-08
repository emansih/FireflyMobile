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

package xyz.hisname.fireflyiii.ui.markdown

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentMarkdownBinding
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class MarkdownFragment: BaseFragment() {

    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private var fragmentMarkdownBinding: FragmentMarkdownBinding? = null
    private val binding get() = fragmentMarkdownBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentMarkdownBinding = FragmentMarkdownBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidget()
        parseText()
        handleClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
                hideKeyboard()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setWidget(){
        binding.discardButton.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_close
                    colorRes = R.color.md_white_1000
                    sizeDp = 12
                },null, null, null)
        binding.doneButton.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_done
                    colorRes = R.color.md_white_1000
                    sizeDp =12
                },null, null, null)
        binding.boldMarkdown.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_bold
                    colorRes = setIconColor()
                    sizeDp = 18
                })
        binding.italicMarkdown.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_italic
                    colorRes = setIconColor()
                    sizeDp = 18
                })
        binding.hyperlinkMarkdown.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_insert_link
                    colorRes = setIconColor()
                    sizeDp = 18
                })
        binding.strikeThroughMarkdown.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_strikethrough
                    colorRes = setIconColor()
                    sizeDp = 18
                })
        binding.quoteMarkdown.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_quote
                    colorRes = setIconColor()
                    sizeDp = 18
                })
        binding.bulletMarkdown.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_list_bulleted
                    colorRes = setIconColor()
                    sizeDp = 18
                })
        binding.numberedListMarkdown.setImageDrawable(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_list_numbered
                    colorRes = setIconColor()
                    sizeDp = 18
                })
        binding.editableText.setText(markdownViewModel.markdownText.value)
        binding.displayText.text = markdownViewModel.markdownText.value
        binding.discardButton.setOnClickListener {
            markdownViewModel.markdownText.postValue("")
            parentFragmentManager.popBackStack()
            hideKeyboard()
        }
        binding.doneButton.setOnClickListener {
            markdownViewModel.markdownText.postValue(binding.editableText.getString())
            parentFragmentManager.popBackStack()
            hideKeyboard()
        }
        binding.discardButton.setBackgroundColor(getCompatColor(R.color.colorPrimary))
        binding.doneButton.setBackgroundColor(getCompatColor(R.color.colorPrimary))
    }

    private fun setIconColor(): Int{
        return if(isDarkMode()){
            R.color.md_white_1000
        } else {
            R.color.md_black_1000
        }
    }

    private fun parseText(){
        binding.editableText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(editable: Editable) {

            }

            override fun beforeTextChanged(charsequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charsequence: CharSequence, start: Int, before: Int, count: Int) {
                binding.displayText.text = charsequence.toString().toMarkDown()
            }

        })

    }

    // https://github.com/k0shk0sh/FastHub/blob/eb13021b9a45ac1ae29815b48247647005a661bd/app/src/main/java/com/fastaccess/provider/markdown/MarkDownProvider.java
    private fun handleClick(){
        binding.boldMarkdown.setOnClickListener {
            val markdownText = binding.editableText.getString()
            val markdownTextStart = binding.editableText.selectionStart
            val markdownTextEnd = binding.editableText.selectionEnd
            val markdownSubString = markdownText.substring(markdownTextStart, markdownTextEnd)
            val result = "**$markdownSubString** "
            binding.editableText.text.replace(markdownTextStart, markdownTextEnd, result)
            binding.editableText.setSelection(result.length + markdownTextStart - 3)
        }
        binding.italicMarkdown.setOnClickListener {
            val markdownText = binding.editableText.getString()
            val markdownTextStart = binding.editableText.selectionStart
            val markdownTextEnd = binding.editableText.selectionEnd
            val markdownSubString = markdownText.substring(markdownTextStart, markdownTextEnd)
            val result = "_" + markdownSubString + "_ "
            binding.editableText.text.replace(markdownTextStart, markdownTextEnd, result)
            binding.editableText.setSelection(result.length + markdownTextStart - 2)
        }
        binding.strikeThroughMarkdown.setOnClickListener {
            val markdownText = binding.editableText.getString()
            val markdownTextStart = binding.editableText.selectionStart
            val markdownTextEnd = binding.editableText.selectionEnd
            val markdownSubString = markdownText.substring(markdownTextStart, markdownTextEnd)
            val result = "~~$markdownSubString~~ "
            binding.editableText.text.replace(markdownTextStart, markdownTextEnd, result)
            binding.editableText.setSelection(result.length + markdownTextStart - 3)
        }
        binding.quoteMarkdown.setOnClickListener {
            val markdownText = binding.editableText.getString()
            val markdownTextStart = binding.editableText.selectionStart
            val markdownTextEnd = binding.editableText.selectionEnd
            val markdownSubString = markdownText.substring(markdownTextStart, markdownTextEnd)
            val result = if (hasNewLine(markdownText, markdownTextStart)) {
                "> $markdownSubString"
            } else {
                "\n> $markdownSubString"
            }
            binding.editableText.text.replace(markdownTextStart, markdownTextEnd, result)
            binding.editableText.setSelection(result.length + markdownTextStart)
        }
        binding.bulletMarkdown.setOnClickListener {
            val markdownText = binding.editableText.getString()
            val markdownTextStart = binding.editableText.selectionStart
            val markdownTextEnd = binding.editableText.selectionEnd
            val markdownSubString = markdownText.substring(markdownTextStart, markdownTextEnd)
            val result = if (hasNewLine(markdownText, markdownTextStart)) {
                "• $markdownSubString"
            } else {
                "\n• $markdownSubString"
            }
            binding.editableText.text.replace(markdownTextStart, markdownTextEnd, result)
            binding.editableText.setSelection(result.length + markdownTextStart)
        }
        binding.hyperlinkMarkdown.setOnClickListener {
            val layoutView = layoutInflater.inflate(R.layout.dialog_hyperlink, null)
            val urlText = layoutView.findViewById<EditText>(R.id.linktextEditText)
            urlText.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
                icon = GoogleMaterial.Icon.gmd_format_underlined
                sizeDp = 16
            },null, null, null)
            val url = layoutView.findViewById<EditText>(R.id.linkEditText)
            url.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
                icon = GoogleMaterial.Icon.gmd_insert_link
                sizeDp = 16
            },null, null, null)
            val alert = AlertDialog.Builder(requireContext())
            alert.apply {
                setTitle(R.string.insert_link)
                setView(layoutView)
                setPositiveButton(android.R.string.ok) { dialogInterface, which ->
                    binding.editableText.append("[" + urlText.getString() + "]" + "(" + url.getString() + ")")
                }
                setNegativeButton(android.R.string.cancel){ dialogInterface, which ->

                }
                show()
            }
        }
        binding.numberedListMarkdown.setOnClickListener {
            val markdownText = binding.editableText.getString()
            val markdownTextStart = binding.editableText.selectionStart
            val markdownTextEnd = binding.editableText.selectionEnd
            val markdownSubString = markdownText.substring(markdownTextStart, markdownTextEnd)
            val result = if (hasNewLine(markdownText, markdownTextStart)) {
                "1. $markdownSubString"
            } else {
                "\n1. $markdownSubString"
            }
            binding.editableText.text.replace(markdownTextStart, markdownTextEnd, result)
            binding.editableText.setSelection(result.length + markdownTextStart)
        }
    }

    private fun hasNewLine(source: String, selectionStart: Int): Boolean{
        try {
            if(source.isEmpty()){
                return true
            }
            val textSource = source.substring(0, selectionStart)
            return textSource[source.length - 1].equals(10)
        } catch(e: StringIndexOutOfBoundsException){
            return false
        }
    }
}