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

package xyz.hisname.fireflyiii.ui.transaction.addtransaction

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.hootsuite.nachos.ChipConfiguration
import com.hootsuite.nachos.chip.ChipCreator
import com.hootsuite.nachos.chip.ChipSpan
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.tokenizer.SpanChipTokenizer
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.*
import me.toptas.fancyshowcase.FancyShowCaseQueue
import net.dinglisch.android.tasker.TaskerPlugin
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentAddTransactionBinding
import xyz.hisname.fireflyiii.ui.markdown.MarkdownViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.search.AccountSearchDialog
import xyz.hisname.fireflyiii.ui.account.search.AccountSearchViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.categories.CategoriesDialog
import xyz.hisname.fireflyiii.ui.categories.CategoriesDialogViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyBottomSheetViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.transaction.search.DescriptionSearch
import xyz.hisname.fireflyiii.ui.transaction.search.DescriptionViewModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File
import java.util.*

class AddTransactionFragment: BaseFragment() {

    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId") ?: 0 }
    private val transactionActivity by lazy { arguments?.getBoolean("FROM_TRANSACTION_ACTIVITY") }
    private val isTasker by lazy { arguments?.getBoolean("isTasker") ?: false }
    private val isFromNotification by lazy { requireActivity().intent.extras?.getBoolean("isFromNotification") ?: false }
    private val description by lazy { requireActivity().intent.extras?.getString("description") ?: "" }
    private val amount by lazy { requireActivity().intent.extras?.getString("amount") ?: "" }
    private val transactionType by lazy { arguments?.getString("transactionType") ?: "" }

    private val addTransactionViewModel by lazy {
        if(isTasker){
            getViewModel(AddTransactionViewModel::class.java)
        } else {
            ViewModelProvider(requireParentFragment()).get(AddTransactionViewModel::class.java)
        }
    }
    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val categorySearch by lazy { getViewModel(CategoriesDialogViewModel::class.java) }
    private val descriptionSearch by lazy { getViewModel(DescriptionViewModel::class.java) }
    private val accountSearchViewModel by lazy { getViewModel(AccountSearchViewModel::class.java) }

    private lateinit var fileUri: Uri
    private var selectedTime = ""
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }

    private var fragmentAddTransactionBinding: FragmentAddTransactionBinding? = null
    private val binding get() = fragmentAddTransactionBinding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAddTransactionBinding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressBar.animateView(binding.addTransactionProgress.progressOverlay, View.VISIBLE, 0.4f, 200)
        if(isTasker){
            addTransactionViewModel.transactionBundle.observe(viewLifecycleOwner) { bundle ->
                addTransactionViewModel.parseBundle(bundle)
            }
            binding.addSplit.isGone = true
            binding.removeSplit.isGone = true
            addTransactionViewModel.isFromTasker.postValue(true)
        }
        if(isFromNotification){
            requireActivity().intent.extras?.let {
                addTransactionViewModel.parseBundle(it)
            }
        }
        setIcons()
        setWidgets()
        if(transactionJournalId != 0L){
            binding.addSplit.isGone = true
            binding.removeSplit.isGone = true
            addTransactionViewModel.getTransactionFromJournalId(transactionJournalId)
            displayAttachment()
        }
        setFab()
        setCalculator()
        contextSwitch()
        setAutofilledFields()
    }

    private fun setAutofilledFields() {
        if (amount != null) {
            binding.transactionAmountEdittext.setText(amount)
        }
        if (description != null) {
            binding.descriptionEdittext.setText(description)
        }
    }

    private fun displayAttachment(){
        addTransactionViewModel.transactionAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        false, { data: AttachmentData ->
                    AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.are_you_sure))
                            .setPositiveButton(android.R.string.ok){ _, _ ->
                                addTransactionViewModel.deleteAttachment(data).observe(viewLifecycleOwner){ isSuccessful ->
                                    if(isSuccessful){
                                        attachmentDataAdapter.remove(data)
                                        binding.attachmentInformation.adapter?.notifyDataSetChanged()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), fileUri) ?: "",
                        "", "", "", 0, "", "", ""), 0))
                attachmentItemAdapter.add(fileUri)
                binding.attachmentInformation.adapter?.notifyDataSetChanged()
                if (transactionJournalId != 0L){
                    toastInfo("Uploading...")
                    addTransactionViewModel.uploadFile(transactionJournalId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
                        // Only show the updated files array if upload succeeds
                        if(workInfo[0].state == WorkInfo.State.SUCCEEDED){
                            binding.attachmentInformation.adapter?.notifyDataSetChanged()
                            toastSuccess("File uploaded")
                        } else {
                            toastError("There was an issue uploading your file", Toast.LENGTH_LONG)
                        }
                    }
                } else {
                    binding.attachmentInformation.adapter?.notifyDataSetChanged()
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
                if (transactionJournalId != 0L){
                    toastInfo("Uploading...")
                    addTransactionViewModel.uploadFile(transactionJournalId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
                        // Only show the updated files array if upload succeeds
                        if(workInfo[0].state == WorkInfo.State.SUCCEEDED){
                            binding.attachmentInformation.adapter?.notifyDataSetChanged()
                            toastSuccess("File uploaded")
                        } else {
                            toastError("There was an issue uploading your file", Toast.LENGTH_LONG)
                        }
                    }
                } else {
                    binding.attachmentInformation.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun showTaskerVariable(editText: EditText, shouldAppend: Boolean = false){
        val variablesFromHost = TaskerPlugin.getRelevantVariableList(requireActivity().intent.extras)
        val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item)
        variablesFromHost.forEach { variables ->
            arrayAdapter.add(variables)
        }
        val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Select Variable")
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
        dialog.setAdapter(arrayAdapter){ _, which ->
            val itemClicked = arrayAdapter.getItem(which)
            if(shouldAppend){
                editText.append(itemClicked + "\n")
            } else {
                editText.setText(itemClicked)
            }
        }
        dialog.show()
    }

    private fun setTaskerIcons(): Drawable? {
        return if(isTasker){
            IconicsDrawable(requireContext()).apply {
                icon = GoogleMaterial.Icon.gmd_label
                colorRes = R.color.colorAccent
                sizeDp = 24
            }
        } else {
            null
        }
    }

    private fun setUi() {
        addTransactionViewModel.transactionCurrency.observe(viewLifecycleOwner) { transactionCurrency ->
            binding.currencyEdittext.setText(transactionCurrency)
        }

        addTransactionViewModel.transactionDescription.observe(viewLifecycleOwner) { transactionDescription ->
            binding.descriptionEdittext.setText(transactionDescription)
        }

        addTransactionViewModel.transactionAmount.observe(viewLifecycleOwner) { transactionAmount ->
            binding.transactionAmountEdittext.setText(transactionAmount)
        }

        addTransactionViewModel.transactionDate.observe(viewLifecycleOwner) { transactionDate ->
            if(transactionDate.isEmpty()){
                binding.transactionDateEdittext.setText(DateTimeUtil.getTodayDate())
            } else {
                binding.transactionDateEdittext.setText(transactionDate)
            }
        }

        addTransactionViewModel.transactionTime.observe(viewLifecycleOwner) { transactionTime ->
            binding.timeEdittext.setText(transactionTime)
        }

        if (binding.piggyExposedMenu.isVisible) {
            addTransactionViewModel.transactionPiggyBank.observe(viewLifecycleOwner) { transactionPiggyBank ->
                binding.piggyExposedDropdown.setText(transactionPiggyBank)

            }
        }

        addTransactionViewModel.transactionTags.observe(viewLifecycleOwner) { transactionTags ->
            binding.tagsChip.setText(transactionTags)
        }

        addTransactionViewModel.transactionBudget.observe(viewLifecycleOwner) { transactionBudget ->
            binding.budgetExposedDropdown.setText(transactionBudget)
        }

        addTransactionViewModel.transactionCategory.observe(viewLifecycleOwner) { transactionCategory ->
            binding.categoryEdittext.setText(transactionCategory)
        }

        addTransactionViewModel.transactionBill.observe(viewLifecycleOwner){ transactionBill ->
            binding.billExposedDropdown.setText(transactionBill)
        }

        addTransactionViewModel.transactionNote.observe(viewLifecycleOwner) { transactionNote ->
            binding.noteEdittext.setText(transactionNote)
        }

        addTransactionViewModel.fileUri.observe(viewLifecycleOwner){ uri ->
            uri.forEach { uriArray ->
                attachmentDataAdapter.add(AttachmentData(Attributes(0, "",
                        "", Uri.EMPTY, FileUtils.getFileName(requireContext(), uriArray) ?: "",
                        "", "", "", 0, "", "", ""), 0))
            }
            attachmentItemAdapter.addAll(uri)
            binding.attachmentInformation.adapter?.notifyDataSetChanged()
        }
    }

    private fun setFab(){
        addTransactionViewModel.saveData.observe(viewLifecycleOwner){
            hideKeyboard()
            val piggyBank = if(binding.piggyExposedDropdown.isBlank()){
                null
            } else {
                binding.piggyExposedDropdown.getString()
            }
            val categoryName = if(binding.categoryEdittext.isBlank()){
                null
            } else {
                binding.categoryEdittext.getString()
            }
            val transactionTags = if(binding.tagsChip.allChips.isNullOrEmpty()){
                null
            } else {
                // Remove [ and ] from beginning and end of string
                val beforeTags = binding.tagsChip.allChips.toString().substring(1)
                beforeTags.substring(0, beforeTags.length - 1)
            }
            val budgetName = if(binding.budgetExposedDropdown.isBlank()){
                null
            } else {
                binding.budgetExposedDropdown.getString()
            }
            val billName = if(binding.billExposedMenu.isGone && binding.billExposedDropdown.isBlank()){
                null
            } else {
                binding.billExposedDropdown.getString()
            }
            var sourceAccount = ""
            var destinationAccount = ""
            when {
                Objects.equals("Withdrawal", transactionType) -> {
                    sourceAccount = binding.sourceExposedDropdown.getString()
                    destinationAccount = binding.destinationEdittext.getString()
                }
                Objects.equals("Transfer", transactionType) -> {
                    sourceAccount = binding.sourceExposedDropdown.getString()
                    destinationAccount = binding.destinationExposedDropdown.getString()
                }
                Objects.equals("Deposit", transactionType) -> {
                    sourceAccount = binding.sourceEdittext.getString()
                    destinationAccount = binding.destinationExposedDropdown.getString()
                }
            }
            if(transactionJournalId != 0L){
                updateData(piggyBank, sourceAccount, destinationAccount, categoryName,
                        transactionTags, billName, budgetName)
            } else {
                if(isTasker){
                    taskerPlugin(piggyBank, sourceAccount, destinationAccount, categoryName, transactionTags,
                            billName, budgetName)
                } else {
                    submitData(piggyBank, sourceAccount, destinationAccount, categoryName,
                            transactionTags, billName, budgetName)
                }

            }
        }
    }

    private fun taskerPlugin(piggyBank: String?, sourceAccount: String, destinationAccount: String,
                             categoryName: String?, transactionTags: String?, billName: String?, budgetName: String?){
        val currencyText = binding.currencyEdittext.getString()
        if(currencyText.startsWith("%")){
            addTransactionViewModel.currency = binding.currencyEdittext.getString()
        } else {
            /* Get content between brackets
             * For example: Euro(EUR) becomes (EUR)
             * Then we remove the first and last character and it becomes EUR
             */
            addTransactionViewModel.currency = binding.currencyEdittext.getString()
            val regex = "(?<=\\().+?(?=\\))".toRegex()
            val regexReplaced = regex.find(addTransactionViewModel.currency)
            regexReplaced?.value
            addTransactionViewModel.currency = regexReplaced?.value ?: ""
        }
        addTransactionViewModel.transactionType.postValue(transactionType)
        addTransactionViewModel.transactionDescription.postValue(binding.descriptionEdittext.getString())
        addTransactionViewModel.transactionAmount.postValue(binding.transactionAmountEdittext.getString())
        addTransactionViewModel.transactionDate.postValue(binding.transactionDateEdittext.getString())
        addTransactionViewModel.transactionTime.postValue(binding.timeEdittext.getString())
        addTransactionViewModel.transactionPiggyBank.postValue(piggyBank)
        addTransactionViewModel.transactionSourceAccount.postValue(sourceAccount)
        addTransactionViewModel.transactionDestinationAccount.postValue(destinationAccount)
        addTransactionViewModel.transactionCurrency.postValue(addTransactionViewModel.currency)
        addTransactionViewModel.transactionTags.postValue(transactionTags)
        addTransactionViewModel.transactionBudget.postValue(budgetName)
        addTransactionViewModel.transactionCategory.postValue(categoryName)
        addTransactionViewModel.transactionBill.postValue(billName)
        addTransactionViewModel.transactionNote.postValue(binding.noteEdittext.getString())
        addTransactionViewModel.fileUri.postValue(attachmentItemAdapter)
        addTransactionViewModel.removeFragment.postValue(true)
    }

    private fun setIcons(){
        binding.tagsChip.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_tags
            colorRes = R.color.md_green_400
            sizeDp = 24
        },null, setTaskerIcons(), null)

        binding.currencyEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, setTaskerIcons(), null)
        binding.transactionAmountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_calculator
                    colorRes = R.color.md_blue_grey_400
                    sizeDp = 16
                }, null, setTaskerIcons(), null)
        binding.transactionDateEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        },null, setTaskerIcons(), null)
        binding.sourceEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_exchange_alt
            colorRes = R.color.md_green_500
            sizeDp = 24
        },null, setTaskerIcons(), null)
        val bankTransferIconColorWrap = DrawableCompat.wrap(getCompatDrawable(R.drawable.ic_bank_transfer)!!).mutate()
        DrawableCompat.setTint(bankTransferIconColorWrap, Color.parseColor("#e67a15"))
        binding.destinationEdittext.setCompoundDrawablesWithIntrinsicBounds(
                bankTransferIconColorWrap,null, setTaskerIcons(), null)
        binding.categoryEdittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply{
            icon = FontAwesome.Icon.faw_chart_bar
            colorRes = R.color.md_deep_purple_400
            sizeDp = 24
        }, null, setTaskerIcons(), null)
        binding.timeEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_clock
                    colorRes = R.color.md_red_400
                    sizeDp = 24
                },null, setTaskerIcons(), null)
        binding.tagsChip.chipTokenizer = SpanChipTokenizer(requireContext(), object : ChipCreator<ChipSpan> {
            override fun configureChip(chip: ChipSpan, chipConfiguration: ChipConfiguration) {
            }

            override fun createChip(context: Context, text: CharSequence, data: Any?): ChipSpan {
                return ChipSpan(requireContext(), text,
                        IconicsDrawable(requireContext()).apply {
                            icon = FontAwesome.Icon.faw_tag
                            sizeDp = 12
                        }, data)
            }

            override fun createChip(context: Context, existingChip: ChipSpan): ChipSpan {
                return ChipSpan(requireContext(), existingChip)
            }
        }, ChipSpan::class.java)
        binding.descriptionEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_description
                    colorRes = R.color.md_amber_300
                    sizeDp = 20
                },null, setTaskerIcons(), null)
        if(isTasker){
            binding.tagsLayout.endIconDrawable = setTaskerIcons()
            binding.noteEdittext.setCompoundDrawablesWithIntrinsicBounds(
                    null,null, setTaskerIcons(), null)
        }
    }

    private fun setCalculator(){
        binding.transactionAmountEdittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= binding.transactionAmountEdittext.compoundDrawables[0].bounds.width() + 30){
                        addTransactionViewModel.transactionAmount.value = if(binding.transactionAmountEdittext.getString().isEmpty()){
                            "0.0"
                        } else {
                            binding.transactionAmountEdittext.getString()
                        }
                        val calculatorDialog = TransactionCalculatorDialog()
                        calculatorDialog.show(parentFragmentManager, "calculatorDialog")
                        return true
                    } else if(binding.transactionAmountEdittext.compoundDrawables[2] != null &&
                            event.rawX >= (binding.transactionAmountEdittext.right -
                                    binding.transactionAmountEdittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(binding.transactionAmountEdittext)
                    }
                }
                return false
            }
        })
        addTransactionViewModel.transactionAmount.observe(viewLifecycleOwner){ amount ->
            binding.transactionAmountEdittext.setText(amount)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setWidgets(){
        val queue = FancyShowCaseQueue()
        if(transactionActivity == true){
            queue.add(showCase(R.string.transactions_create_switch_box, "bottomNavigationShowCase",
                           requireActivity().findViewById(R.id.transactionBottomView), false))
        }
        queue.add(showCase(R.string.urge_users_to_click_icons, "transactionIcons",
                binding.transactionAmountPlaceholderView))
        queue.show()
        binding.addAttachmentButton.setOnClickListener {
            attachmentDialog()
        }
        binding.transactionDateEdittext.setText(DateTimeUtil.getTodayDate())
        binding.transactionDateEdittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.transactionDateEdittext.performClick()
                if (binding.transactionDateEdittext.compoundDrawables[2] != null &&
                        event.rawX >= (binding.transactionDateEdittext.right -
                                binding.transactionDateEdittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(binding.transactionDateEdittext)
                } else {
                    val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    val picker = materialDatePicker.build()
                    picker.show(parentFragmentManager, picker.toString())
                    picker.addOnPositiveButtonClickListener { time ->
                        binding.transactionDateEdittext.setText(DateTimeUtil.getCalToString(time.toString()))
                    }
                }
            }
            false
        }
        binding.tagsChip.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.tagsChip.performClick()
                if (binding.tagsChip.compoundDrawables[2] != null &&
                        event.rawX >= (binding.tagsChip.right -
                                binding.tagsChip.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(binding.tagsChip, true)
                }
            }
            false
        }
        binding.categoryEdittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= binding.categoryEdittext.compoundDrawables[0].bounds.width() + 30){
                        val catDialog = CategoriesDialog()
                        catDialog.show(parentFragmentManager, "categoryDialog")
                        return true
                    } else if(binding.categoryEdittext.compoundDrawables[2] != null &&
                            event.rawX >= (binding.categoryEdittext.right -
                                    binding.categoryEdittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(binding.categoryEdittext)
                    }
                }
                return false
            }
        })
        categorySearch.categoryName.observe(viewLifecycleOwner) {
            binding.categoryEdittext.setText(it)
        }
        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currency ->
            addTransactionViewModel.currency = currency
        }
        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) {
            binding.currencyEdittext.setText(it)
        }
        binding.currencyEdittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(binding.currencyEdittext.compoundDrawables[2] != null &&
                            event.rawX >= (binding.currencyEdittext.right -
                                    binding.currencyEdittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(binding.currencyEdittext)
                        addTransactionViewModel.currency = binding.currencyEdittext.getString()
                    } else {
                        CurrencyListBottomSheet().show(parentFragmentManager, "currencyList" )
                    }
                }
                return true
            }
        })
        binding.tagsChip.addChipTerminator('\n' ,ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        binding.tagsChip.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR)
        binding.tagsChip.enableEditChipOnTouch(false, true)
        binding.tagsChip.doAfterTextChanged { editable ->
            addTransactionViewModel.getTags(editable.toString()).observe(viewLifecycleOwner){ tags ->
                val tagsAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, tags)
                binding.tagsChip.threshold = 1
                binding.tagsChip.setAdapter(tagsAdapter)
            }
        }
        if(isTasker){
            binding.sourceExposedMenu.endIconDrawable = setTaskerIcons()
            binding.sourceExposedMenu.setEndIconOnClickListener {
                showTaskerVariable(binding.sourceExposedDropdown)
            }
            binding.destinationExposedMenu.endIconDrawable = setTaskerIcons()
            binding.destinationExposedMenu.setEndIconOnClickListener {
                showTaskerVariable(binding.destinationExposedDropdown)
            }
            binding.budgetExposedMenu.endIconDrawable = setTaskerIcons()
            binding.budgetExposedMenu.setEndIconOnClickListener {
                showTaskerVariable(binding.budgetExposedDropdown)
            }
            binding.piggyExposedMenu.endIconDrawable = setTaskerIcons()
            binding.piggyExposedMenu.setEndIconOnClickListener {
                showTaskerVariable(binding.piggyExposedDropdown)
            }
            binding.billExposedMenu.endIconDrawable = setTaskerIcons()
            binding.billExposedMenu.setEndIconOnClickListener{
                showTaskerVariable(binding.billExposedDropdown)
            }
        }
        addTransactionViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
            if(!isTasker){
                binding.currencyEdittext.setText(defaultCurrency)
            } else if(!binding.currencyEdittext.getString().startsWith("%")) {
                // Is not a tasker variable
                binding.currencyEdittext.setText(defaultCurrency)
            }
        }

        addTransactionViewModel.getAllBills().observe(viewLifecycleOwner){ budget ->
            val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, budget)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.billExposedDropdown.setAdapter(spinnerAdapter)
        }

        addTransactionViewModel.getPiggyBank().observe(viewLifecycleOwner){ budget ->
            val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, budget)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.piggyExposedDropdown.setAdapter(spinnerAdapter)
        }

        addTransactionViewModel.getBudget().observe(viewLifecycleOwner){ budget ->
            val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, budget)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.budgetExposedDropdown.setAdapter(spinnerAdapter)
        }

        binding.destinationEdittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= binding.destinationEdittext.compoundDrawables[0].bounds.width() + 30){
                        val accountDialog = AccountSearchDialog()
                        accountDialog.arguments = bundleOf("accountType" to "expense")
                        accountDialog.show(parentFragmentManager, "accountDialog")
                        accountSearchViewModel.accountName.observe(viewLifecycleOwner){ account ->
                            binding.destinationEdittext.setText(account)
                        }
                        return true
                    } else if(binding.destinationEdittext.compoundDrawables[2] != null &&
                            event.rawX >= (binding.destinationEdittext.right -
                                    binding.destinationEdittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(binding.destinationEdittext)
                    }
                }
                return false
            }
        })

        binding.sourceEdittext.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if(event.action == MotionEvent.ACTION_UP){
                    if(event.x <= binding.sourceEdittext.compoundDrawables[0].bounds.width() + 30){
                        val accountDialog = AccountSearchDialog()
                        accountDialog.arguments = bundleOf("accountType" to "revenue")
                        accountDialog.show(parentFragmentManager, "accountDialog")
                        accountSearchViewModel.accountName.observe(viewLifecycleOwner){ account ->
                            binding.sourceEdittext.setText(account)
                        }
                        return true
                    } else if(binding.sourceEdittext.compoundDrawables[2] != null &&
                            event.rawX >= (binding.sourceEdittext.right -
                                    binding.sourceEdittext.compoundDrawables[2].bounds.width())){
                        showTaskerVariable(binding.sourceEdittext)
                    }
                }
                return false
            }
        })
        binding.expansionLayout.addListener { _, expanded ->
            if(expanded){
                if (binding.piggyExposedMenu.isVisible){
                    binding.dialogAddTransactionLayout.post {
                        binding.dialogAddTransactionLayout.smoothScrollTo(0, binding.piggyExposedMenu.bottom)
                    }
                    binding.dialogAddTransactionLayout.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                        if(scrollY == binding.piggyExposedMenu.bottom){
                            showCase(R.string.transactions_create_transfer_ffInput_piggy_bank_id,
                                    "transactionPiggyShowCase", binding.piggyExposedMenu, false).show()
                        }
                    }
                }
            }
        }
        binding.timeEdittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.timeEdittext.performClick()
                if (binding.timeEdittext.compoundDrawables[2] != null &&
                        event.rawX >= (binding.timeEdittext.right -
                                binding.timeEdittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(binding.timeEdittext)
                } else {
                    val materialTimePicker = MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .build()
                    materialTimePicker.show(parentFragmentManager, "timePickerDialog")
                    materialTimePicker.addOnPositiveButtonClickListener { _ ->
                        val min = if(materialTimePicker.minute < 10){
                            "0${materialTimePicker.minute}"
                        } else {
                            materialTimePicker.minute.toString()
                        }
                        val hour = if(materialTimePicker.hour < 10){
                            "0${materialTimePicker.hour}"
                        } else {
                            materialTimePicker.hour.toString()
                        }
                        selectedTime = "${hour}:${min}"
                        binding.timeEdittext.setText(selectedTime)
                    }
                }
            }
            false
        }
        binding.descriptionEdittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.descriptionEdittext.performClick()
                if (binding.descriptionEdittext.compoundDrawables[2] != null &&
                        event.rawX >= (binding.descriptionEdittext.right -
                                binding.descriptionEdittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(binding.descriptionEdittext)
                } else if(event.x <= binding.descriptionEdittext.compoundDrawables[0].bounds.width() + 30){
                    val transactionDescription = DescriptionSearch()
                    transactionDescription.show(parentFragmentManager, "descriptionDialog")
                    descriptionSearch.transactionName.observe(viewLifecycleOwner){ search ->
                        binding.descriptionEdittext.setText(search)
                    }
                }
            }
            false
        }
        binding.descriptionEdittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getTransactionByDescription(editable.toString()).observe(viewLifecycleOwner){ list ->
                val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, list)
                binding.descriptionEdittext.setAdapter(adapter)
            }
        }
        binding.destinationEdittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getAccountByNameAndType("expense", editable.toString()).observe(viewLifecycleOwner){ list ->
                val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, list)
                binding.destinationEdittext.setAdapter(autocompleteAdapter)
            }
        }
        binding.categoryEdittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getCategory(editable.toString()).observe(viewLifecycleOwner) { dataToDisplay ->
                val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, dataToDisplay)
                binding.categoryEdittext.setAdapter(autocompleteAdapter)
            }
        }
        binding.sourceEdittext.doAfterTextChanged { editable ->
            addTransactionViewModel.getAccountByNameAndType("revenue", editable.toString()).observe(viewLifecycleOwner){ list ->
                val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, list)
                binding.sourceEdittext.setAdapter(autocompleteAdapter)
            }
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            binding.noteEdittext.setText(markdownText)
        }
        binding.noteEdittext.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.noteEdittext.performClick()
                if (binding.noteEdittext.compoundDrawables[2] != null &&
                        event.rawX >= (binding.noteEdittext.right -
                                binding.noteEdittext.compoundDrawables[2].bounds.width())) {
                    showTaskerVariable(binding.noteEdittext)
                } else {
                    markdownViewModel.markdownText.postValue(binding.noteEdittext.getString())
                    parentFragmentManager.commit {
                        replace(R.id.transactionPagerRoot, MarkdownFragment())
                        addToBackStack(null)
                    }
                }
            }
            true
        }
        binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
        binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            binding.attachmentInformation.adapter?.notifyDataSetChanged()
        }) { another: Int -> }
        setUi()
        binding.addSplit.setOnClickListener {
            addTransactionViewModel.increaseTab.postValue(true)
        }
        binding.removeSplit.setOnClickListener {
            addTransactionViewModel.decreaseTab.postValue(true)
        }
        if(addTransactionViewModel.numTabs > 1){
            binding.removeSplit.isVisible = true
        }
    }

    private fun contextSwitch(){
        addTransactionViewModel.getAccounts().observe(viewLifecycleOwner){ accounts ->
            if(transactionType.contentEquals("Transfer")){
                binding.sourceExposedMenu.isVisible = true
                binding.sourceLayout.isGone = true
                binding.destinationLayout.isGone = true
                binding.destinationExposedMenu.isVisible = true
                binding.piggyExposedMenu.isVisible = true
                binding.budgetExposedMenu.isGone = true
                val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, accounts)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.destinationExposedDropdown.setAdapter(spinnerAdapter)
                binding.sourceExposedDropdown.setAdapter(spinnerAdapter)
                addTransactionViewModel.transactionDestinationAccount.observe(viewLifecycleOwner){ transactionDestinationAccount ->
                    binding.destinationExposedDropdown.setText(transactionDestinationAccount)
                }
                addTransactionViewModel.transactionSourceAccount.observe(viewLifecycleOwner){ transactionSourceAccount ->
                    binding.sourceExposedDropdown.setText(transactionSourceAccount)
                }
            } else if(transactionType.contentEquals("Deposit")){
                val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, accounts)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.destinationExposedDropdown.setAdapter(spinnerAdapter)
                binding.destinationLayout.isGone = true
                binding.sourceExposedMenu.isGone = true
                binding.budgetExposedMenu.isGone = true
                binding.billExposedMenu.isGone = true
                addTransactionViewModel.transactionDestinationAccount.observe(viewLifecycleOwner){ transactionDestinationAccount ->
                    binding.destinationExposedDropdown.setText(transactionDestinationAccount)
                }
                addTransactionViewModel.transactionSourceAccount.observe(viewLifecycleOwner){ transactionSourceAccount ->
                    binding.sourceEdittext.setText(transactionSourceAccount)
                }
            } else if(transactionType.contentEquals("Withdrawal")){
                binding.sourceLayout.isGone = true
                binding.destinationExposedMenu.isGone = true
                // Spinner for source account
                val spinnerAdapter = ArrayAdapter(requireContext(),
                        R.layout.cat_exposed_dropdown_popup_item, accounts)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.sourceExposedDropdown.setAdapter(spinnerAdapter)
                addTransactionViewModel.transactionDestinationAccount.observe(viewLifecycleOwner){ transactionDestinationAccount ->
                    binding.destinationEdittext.setText(transactionDestinationAccount)
                }
                addTransactionViewModel.transactionSourceAccount.observe(viewLifecycleOwner){ transactionSourceAccount ->
                    binding.sourceExposedDropdown.setText(transactionSourceAccount)
                }
            }
            ProgressBar.animateView(binding.addTransactionProgress.progressOverlay, View.GONE, 0f, 200)
        }
    }

    private fun submitData(piggyBank: String?, sourceAccount: String, destinationAccount: String,
                           categoryName: String?, transactionTags: String?, billName: String?, budgetName: String?) {
        addTransactionViewModel.addTransaction(transactionType, binding.descriptionEdittext.getString(),
                binding.transactionDateEdittext.getString(), selectedTime, piggyBank, binding.transactionAmountEdittext.getString(),
                sourceAccount, destinationAccount, categoryName, transactionTags, budgetName, billName,
                attachmentItemAdapter, binding.noteEdittext.getString())
    }

    private fun updateData(piggyBank: String?, sourceAccount: String, destinationAccount: String,
                           categoryName: String?, transactionTags: String?, billName: String?, budgetName: String?){
        ProgressBar.animateView(binding.addTransactionProgress.progressOverlay, View.VISIBLE, 0.4f, 200)
        addTransactionViewModel.updateTransaction(transactionJournalId, transactionType,
                binding.descriptionEdittext.getString(), binding.transactionDateEdittext.getString(),
                selectedTime, piggyBank, binding.transactionAmountEdittext.getString(),
                sourceAccount, destinationAccount, categoryName,
                transactionTags, budgetName, billName, binding.noteEdittext.getString()).observe(viewLifecycleOwner){ response ->
            addTransactionViewModel.isLoading.postValue(false)
            ProgressBar.animateView(binding.addTransactionProgress.progressOverlay, View.GONE, 0f, 200)
            if(response.first){
                toastSuccess(response.second)
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                toastInfo(response.second)
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

    override fun onDestroy() {
        super.onDestroy()
        markdownViewModel.markdownText.postValue("")
    }
}