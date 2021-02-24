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

package xyz.hisname.fireflyiii.ui.budget

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_add_budget.*
import kotlinx.android.synthetic.main.fragment_add_budget.add_attachment_button
import kotlinx.android.synthetic.main.fragment_add_budget.attachment_information
import kotlinx.android.synthetic.main.fragment_add_budget.placeHolderToolbar
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.repository.budget.BudgetType
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyBottomSheetViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel
import java.io.File
import java.util.*

class AddBudgetFragment: BaseAddObjectFragment() {

    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val addBudgetViewModel by lazy { getImprovedViewModel(AddBudgetViewModel::class.java) }
    private lateinit var fileUri: Uri
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private val attachmentDataAdapter by lazy { arrayListOf<AttachmentData>() }
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }
    private val budgetId by lazy { arguments?.getLong("budgetId") ?: 0L }
    private val currencySymbol by lazy { arguments?.getString("currencySymbol") ?:"" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_budget, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Remove this when the time is right
        addBudgetViewModel.unSupportedVersion.observe(viewLifecycleOwner){ unSupported ->
            if(unSupported){
                val message = Html.fromHtml("You are using an unsupported version of Firefly III which contains a bug. Proceed at your own risk. Follow <a href=\"https://github.com/firefly-iii/firefly-iii/issues/4394\">this issue</a> for more info")
                val layoutInflater = LayoutInflater.from(requireContext())
                val checkbox = layoutInflater.inflate(R.layout.dialog_checkbox, null)
                val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Uh oh...")
                        .setMessage(message)
                        .setView(checkbox)
                        .setPositiveButton("OK"){ _ , _ ->
                            val checkBox = checkbox.findViewById<CheckBox>(R.id.doNotShow)
                            addBudgetViewModel.doNotShowAgain(checkBox.isChecked)
                        }
                        .show()
                dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
            }
        }
        if(budgetId != 0L){
            updateBudgetUi()
        }
    }

    private fun updateBudgetUi(){
        zipLiveData(addBudgetViewModel.getBudgetById(budgetId, currencySymbol),
                addBudgetViewModel.budgetLimitAttributesLiveData).observe(viewLifecycleOwner) { budget ->
            val budgetAttributes = budget.first.budgetListAttributes
            budgetNameEditText.setText(budgetAttributes.name)
            when (budgetAttributes.auto_budget_type) {
                BudgetType.NONE -> {
                    autoBudget.setSelection(0, true)
                }
                BudgetType.ROLLOVER -> {
                    autoBudget.setSelection(1, true)
                }
                else -> {
                    autoBudget.setSelection(2, true)
                }
            }
            val budgetPeriod = budgetAttributes.auto_budget_period
            if (budgetPeriod.contentEquals("weekly")) {
                autoBudgetPeriod.setSelection(0, true)
            } else if (budgetPeriod.contentEquals("monthly")) {
                autoBudgetPeriod.setSelection(1, true)
            } else if (budgetPeriod.contentEquals("quarterly")) {
                autoBudgetPeriod.setSelection(2, true)
            } else if (budgetPeriod.contentEquals("half-yearly")) {
                autoBudgetPeriod.setSelection(3, true)
            } else {
                autoBudgetPeriod.setSelection(4, true)
            }

            val budgetLimitAttribute = budget.second.attributes
            amountEdittext.setText(budgetLimitAttribute.amount.toString())
            addBudgetViewModel.currency = budgetLimitAttribute.currency_code
            currencyEdittext.setText(budgetLimitAttribute.currency_name + " (" + budgetLimitAttribute.currency_code + ")")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachment_information.isVisible = true
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0))
                attachmentItemAdapter.add(fileUri)
                attachment_information.adapter?.notifyDataSetChanged()
            }
        }
        chooseDocument = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){ fileChoosen ->
            attachment_information.isVisible = true
            if(fileChoosen != null){
                fileChoosen.forEach { file ->
                    attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                            "", Uri.EMPTY, FileUtils.getFileName(requireContext(), file) ?: "",
                            "", "", "", 0, "", "", ""), 0))
                }
                attachmentItemAdapter.addAll(fileChoosen)
                attachment_information.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun setIcons() {
        currencyEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        amountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_dollar_sign
                    colorRes = R.color.md_yellow_A700
                    sizeDp = 24
                },null, null, null)
        if(budgetId == 0L){
            addBudgetFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
                icon = FontAwesome.Icon.faw_plus
                colorRes = R.color.md_black_1000
                sizeDp = 24
            })
        } else {
            addBudgetFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
                icon = GoogleMaterial.Icon.gmd_update
                colorRes = R.color.md_black_1000
                sizeDp = 24
            })
        }

    }

    override fun setWidgets() {
        disableField()
        autoBudget.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0){
                    autoBudgetPeriod.isVisible = true
                    amountLayout.isVisible = true
                    currencyLayout.isVisible = true
                } else {
                    disableField()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        setCurrency()
        placeHolderToolbar.setNavigationOnClickListener { handleBack() }
        addBudgetFab.setOnClickListener {
            submitData()
        }
        add_attachment_button.setOnClickListener {
            val listItems = arrayOf(getString(R.string.capture_image_from_camera), getString(R.string.choose_file))
            AlertDialog.Builder(requireContext())
                    .setItems(listItems) { dialog, which ->
                        when (which) {
                            0 -> {
                                val createTempDir = File(requireContext().getExternalFilesDir(null).toString() +
                                        File.separator + "temp")
                                if(!createTempDir.exists()){
                                    createTempDir.mkdir()
                                }
                                val randomId = UUID.randomUUID().toString().substring(0, 7)
                                val fileToOpen = File(requireContext().getExternalFilesDir(null).toString() +
                                        File.separator + "temp" + File.separator + "${randomId}-firefly.png")
                                if(fileToOpen.exists()){
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
        attachment_information.layoutManager = LinearLayoutManager(requireContext())
        attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        attachment_information.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            attachment_information.adapter?.notifyDataSetChanged()
        }) { another: Int -> }
    }

    override fun submitData() {
        ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
        var amount: String? = null
        var currency: String? = null
        var freq: String? = null
        if(autoBudget.selectedItemPosition != 0){
            amount =  amountEdittext.getString()
            currency = addBudgetViewModel.currency
            freq = when(autoBudgetPeriod.selectedItemPosition){
                0 -> "weekly"
                1 -> "monthly"
                2 -> "quarterly"
                3 -> "half-year"
                4 -> "yearly"
                else -> ""
            }
        }
        val budgetType = when (autoBudget.selectedItemPosition) {
            1 -> {
                BudgetType.RESET
            }
            2 -> {
                BudgetType.ROLLOVER
            }
            else -> {
                BudgetType.NONE
            }
        }
        if(budgetId == 0L){
            addBudgetViewModel.addBudget(budgetNameEditText.getString(), budgetType, currency,
                    amount, freq, attachmentItemAdapter).observe(viewLifecycleOwner) { response ->
                if(response.first){
                    ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                    toastSuccess(response.second)
                    handleBack()
                } else {
                    ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                    toastInfo(response.second)
                }
            }
        } else {
            addBudgetViewModel.updateBudget(budgetId, budgetNameEditText.getString(), budgetType, currency,
                    amount, freq).observe(viewLifecycleOwner) { response ->
                if(response.first){
                    ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                    toastSuccess(response.second)
                    handleBack()
                } else {
                    ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                    toastInfo(response.second)
                }
            }
        }

    }

    private fun setCurrency(){
        currencyEdittext.setOnClickListener {
            if(autoBudget.selectedItemPosition != 0){
                CurrencyListBottomSheet().show(childFragmentManager, "currencyList" )
            }
        }

        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currencyCode ->
            addBudgetViewModel.currency = currencyCode
        }

        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) { currency ->
            currencyEdittext.setText(currency)

        }
        if(budgetId == 0L){
            addBudgetViewModel.getDefaultCurrency().observe(viewLifecycleOwner){ currency ->
                currencyEdittext.setText(currency)
            }
        }

    }

    private fun disableField(){
        autoBudgetPeriod.isVisible = false
        amountLayout.isVisible = false
        currencyLayout.isVisible = false
    }

    private fun handleBack() {
        unReveal(addBudgetLayout)
    }
}