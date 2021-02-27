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

package xyz.hisname.fireflyiii.ui.piggybank

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_add_piggy.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.MarkdownViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File
import java.util.*

class AddPiggyFragment: BaseAddObjectFragment() {

    private var currentAmount: String? = null
    private var startDate: String? = null
    private var targetDate: String? = null
    private var notes: String? = null
    private var groupTitle: String? = null
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val piggyViewModel by lazy { getImprovedViewModel(AddPiggyViewModel::class.java) }
    private val piggyId by lazy { arguments?.getLong("piggyId") ?: 0 }
    private lateinit var fileUri: Uri
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_piggy, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_piggy_layout)
        updateEditText()
        setFab()
        showHelpText()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0))
                attachmentItemAdapter.add(fileUri)
                if (piggyId != 0L){
                    toastInfo("Uploading...")
                    piggyViewModel.uploadFile(piggyId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
                        // Only show the updated files array if upload succeeds
                        if(workInfo[0].state == WorkInfo.State.SUCCEEDED){
                            attachment_information.adapter?.notifyDataSetChanged()
                            toastSuccess("File uploaded")
                        } else {
                            toastError("There was an issue uploading your file", Toast.LENGTH_LONG)
                        }
                    }
                } else {
                    attachment_information.adapter?.notifyDataSetChanged()
                }
            }
        }
        chooseDocument = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){ fileChoosen ->
            if(fileChoosen != null){
                fileChoosen.forEach { file ->
                    attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                            "", Uri.EMPTY, FileUtils.getFileName(requireContext(), file) ?: "",
                            "", "", "", 0, "", "", ""), 0))
                }
                attachmentItemAdapter.addAll(fileChoosen)
                if (piggyId != 0L){
                    toastInfo("Uploading...")
                    piggyViewModel.uploadFile(piggyId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
                        // Only show the updated files array if upload succeeds
                        if(workInfo[0].state == WorkInfo.State.SUCCEEDED){
                            attachment_information.adapter?.notifyDataSetChanged()
                            toastSuccess("File uploaded")
                        } else {
                            toastError("There was an issue uploading your file", Toast.LENGTH_LONG)
                        }
                    }
                } else {
                    attachment_information.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun attachmentDialog(){
        val listItems = arrayOf(getString(R.string.capture_image_from_camera), getString(R.string.choose_file))
        AlertDialog.Builder(requireContext())
                .setItems(listItems) { dialog, which ->
                    when (which) {
                        0 -> {
                            val createTempDir = File(requireContext().getExternalFilesDir(null).toString() +
                                    File.separator + "temp")
                            if (!createTempDir.exists()) {
                                createTempDir.mkdir()
                            }
                            val randomId = UUID.randomUUID().toString().substring(0, 7)
                            val fileToOpen = File(requireContext().getExternalFilesDir(null).toString() +
                                    File.separator + "temp" + File.separator + "${randomId}-firefly.png")
                            if (fileToOpen.exists()) {
                                fileToOpen.delete()
                            }
                            fileUri = FileProvider.getUriForFile(requireContext(),
                                    requireContext().packageName + ".provider", fileToOpen)
                            takePicture.launch(fileUri)
                        }
                        1 -> {
                            chooseDocument.launch(arrayOf("*/*"))
                        }
                    }
                }
                .show()
    }

    private fun showHelpText() = showCase(R.string.piggy_bank_description_help_text,
            "descriptionCaseView", description_edittext).show()

    private fun updateEditText(){
        if(piggyId != 0L){
            piggyViewModel.getPiggyById(piggyId).observe(viewLifecycleOwner) { piggyData ->
                val piggyAttributes = piggyData.piggyAttributes
                description_edittext.setText(piggyAttributes.name)
                target_amount_edittext.setText(piggyAttributes.target_amount.toString())
                current_amount_edittext.setText(piggyAttributes.current_amount.toString())
                date_started_edittext.setText(piggyAttributes.start_date)
                date_target_edittext.setText(piggyAttributes.target_date)
                note_edittext.setText(piggyAttributes.notes)
                account_exposed_dropdown.setText(piggyData.piggyAttributes.account_name)
                displayAttachment()
            }
        }
    }

    private fun displayAttachment(){
        piggyViewModel.piggyAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                attachment_information.layoutManager = LinearLayoutManager(requireContext())
                attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                attachment_information.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        false, { data: AttachmentData ->
                    AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.are_you_sure))
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                piggyViewModel.deleteAttachment(data).observe(viewLifecycleOwner) { isSuccessful ->
                                    if (isSuccessful) {
                                        attachmentDataAdapter.remove(data)
                                        attachment_information.adapter?.notifyDataSetChanged()
                                        toastSuccess("Deleted " + data.attachmentAttributes.filename)
                                    } else {
                                        toastError("There was an issue deleting " + data.attachmentAttributes.filename, Toast.LENGTH_LONG)
                                    }
                                }
                            }
                            .show()
                }) { another: Int -> }
            }
        }
    }

    private fun setAccordion(){
        expansionLayout.addListener { _, expanded ->
            if(expanded){
                showCase(R.string.piggy_bank_date_help_text,
                        "dateStartedCaseView", date_started_edittext).show()
            }
        }
    }

    private fun setFab(){
        if(piggyId != 0L){
            addPiggyFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        addPiggyFab.setOnClickListener {
            hideKeyboard()
            currentAmount = if (current_amount_edittext.isBlank()) {
                null
            } else {
                current_amount_edittext.getString()
            }
            startDate = if (date_started_edittext.isBlank()) {
                null
            } else {
                date_started_edittext.getString()
            }
            targetDate = if (date_target_edittext.isBlank()) {
                null
            } else {
                date_target_edittext.getString()
            }
            notes = if (note_edittext.isBlank()) {
                null
            } else {
                note_edittext.getString()
            }
            groupTitle = if(groupEdittext.isBlank()){
                null
            } else {
                groupEdittext.getString()
            }
            if(piggyId == 0L) {
                submitData()
            } else {
                updatePiggyBank()
            }
        }
    }

    override fun setIcons(){
        target_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                }, null, null, null)
        current_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                }, null, null, null)
        date_started_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        }, null, null, null)
        date_target_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        }, null, null, null)
        addPiggyFab.setBackgroundColor(getCompatColor(R.color.colorPrimaryDark))
        addPiggyFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
    }

    override fun setWidgets(){
        setAccordion()
        date_target_edittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(childFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                date_target_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        date_started_edittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(childFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                date_started_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        piggyViewModel.getAccount().observe(viewLifecycleOwner) { list ->
            val accountAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, list)
            accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            account_exposed_dropdown.setAdapter(accountAdapter)
        }
        placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        note_edittext.setOnClickListener {
            markdownViewModel.markdownText.postValue(note_edittext.getString())
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, MarkdownFragment())
                addToBackStack(null)
            }
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            note_edittext.setText(markdownText)
        }
        piggyViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        add_attachment_button.setOnClickListener {
            attachmentDialog()
        }
        attachment_information.layoutManager = LinearLayoutManager(requireContext())
        attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        attachment_information.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            attachment_information.adapter?.notifyDataSetChanged()
        }) { another: Int -> }
        // https://github.com/firefly-iii/firefly-iii/issues/4435
        piggyViewModel.unSupportedVersion.observe(viewLifecycleOwner){ isNotSupported ->
            if(isNotSupported){
                 groupLayout.isGone = true
            } else {
                groupLayout.isVisible = true
            }
        }
    }

    override fun submitData(){
        piggyViewModel.addPiggyBank(description_edittext.getString(),
                account_exposed_dropdown.getString(), currentAmount, notes, startDate,
                target_amount_edittext.getString(), targetDate, groupTitle,
                attachmentItemAdapter).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }

    }

    private fun updatePiggyBank(){
        piggyViewModel.updatePiggyBank(piggyId, description_edittext.getString(), account_exposed_dropdown.getString(),
                currentAmount, notes, startDate, target_amount_edittext.getString(),
                targetDate, groupTitle).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun handleBack() {
        unReveal(dialog_add_piggy_layout)
        markdownViewModel.markdownText.postValue("")
    }
}