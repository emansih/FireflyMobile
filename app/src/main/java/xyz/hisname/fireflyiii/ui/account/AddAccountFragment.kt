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

package xyz.hisname.fireflyiii.ui.account

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import me.toptas.fancyshowcase.FancyShowCaseQueue
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.FragmentAddAccountBinding
import xyz.hisname.fireflyiii.ui.markdown.MarkdownViewModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.attachment.Attributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyBottomSheetViewModel
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.FileUtils
import xyz.hisname.fireflyiii.util.extension.*
import java.io.File
import java.util.*

class AddAccountFragment: BaseAddObjectFragment() {

    private val accountType: String by lazy { arguments?.getString("accountType") ?: "" }
    private val accountId: Long by lazy { arguments?.getLong("accountId") ?: 0L }
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val currencyViewModel by lazy { getViewModel(CurrencyBottomSheetViewModel::class.java) }
    private val accountViewModel by lazy { getImprovedViewModel(AddAccountViewModel::class.java)}
    private lateinit var fileUri: Uri
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var chooseDocument: ActivityResultLauncher<Array<String>>
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private val attachmentItemAdapter by lazy { arrayListOf<Uri>() }
    private var fragmentAddAccountBinding: FragmentAddAccountBinding? = null
    private val binding get() = fragmentAddAccountBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentAddAccountBinding = FragmentAddAccountBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(binding.addAccountLayout)
        updateData()
        binding.placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        if(accountId != 0L){
            binding.addAccountFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        binding.addAccountFab.setOnClickListener {
            submitData()
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
                if (accountId != 0L){
                    toastInfo("Uploading...")
                    accountViewModel.uploadFile(accountId, attachmentItemAdapter).observe(viewLifecycleOwner){ workInfo ->
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
                binding.attachmentInformation.adapter?.notifyDataSetChanged()
                if (accountId != 0L){
                    toastInfo("Uploading...")
                    accountViewModel.uploadFile(accountId, attachmentItemAdapter)
                }
            }
        }
    }

    override fun setIcons() {
        binding.currencyEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        binding.startAmountEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_hourglass_start
                    sizeDp = 24
                    colorRes = R.color.md_red_400
                },null, null, null)
        binding.startDateEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_calendar
                    colorRes = R.color.md_blue_400
                    sizeDp = 24
                },null, null, null)
        binding.interestEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_percent
                    colorRes = R.color.md_amber_700
                    sizeDp = 24
                },null, null, null)
        binding.ibanEdittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = GoogleMaterial.Icon.gmd_format_list_numbered
                    colorRes = R.color.md_deep_orange_900
                    sizeDp = 24
                },null, null, null)
        binding.bicEdittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = GoogleMaterial.Icon.gmd_transfer_within_a_station
                                colorRes = R.color.md_deep_orange_400
                                sizeDp = 24
                            },null, null, null)
        binding.accountNumberEdittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = GoogleMaterial.Icon.gmd_confirmation_number
                                colorRes = R.color.md_brown_600
                                sizeDp = 24
                            },null, null, null)
        binding.openingBalanceEdittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = GoogleMaterial.Icon.gmd_open_with
                                colorRes = R.color.md_red_A100
                                sizeDp = 24
                            },null, null, null)
        binding.openingBalanceDateEdittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = FontAwesome.Icon.faw_calendar
                                colorRes = R.color.md_blue_400
                                sizeDp = 24
                            },null, null, null)
        binding.virtualBalanceEdittext.setCompoundDrawablesWithIntrinsicBounds(
                            IconicsDrawable(requireContext()).apply {
                                icon = FontAwesome.Icon.faw_balance_scale
                                colorRes = R.color.md_light_blue_A200
                                sizeDp = 24
                            },null, null, null)
        binding.addAccountFab.setBackgroundColor(getCompatColor(R.color.colorPrimaryDark))
        binding.addAccountFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
        binding.addAttachmentButton.setOnClickListener {
            attachmentDialog()
        }
        binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
        binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                false, { data: AttachmentData ->
            attachmentDataAdapter.remove(data)
            binding.attachmentInformation.adapter?.notifyDataSetChanged()
        }) { another: Int -> }
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

    private fun setAccordion(){
        binding.expansionLayout.addListener { _, expanded ->
            if(expanded){
                showHiddenHelpText()
            }
        }
    }

    override fun setWidgets() {
        setAccordion()
        currencyViewModel.currencyCode.observe(viewLifecycleOwner) { currency ->
            accountViewModel.currency = currency
        }
        currencyViewModel.currencyFullDetails.observe(viewLifecycleOwner) {
            binding.currencyEdittext.setText(it)
        }
        binding.currencyEdittext.setOnClickListener{
            val currencyListFragment = CurrencyListBottomSheet()
            currencyListFragment.show(childFragmentManager, "currencyList" )
        }
        if(accountId == 0L) {
            accountViewModel.getDefaultCurrency().observe(viewLifecycleOwner) { defaultCurrency ->
                val currencyData = defaultCurrency.currencyAttributes
                binding.currencyEdittext.setText(currencyData.name + " (" + currencyData.code + ")")
            }
        }
        if(accountType == "asset"){
            binding.openingBalanceDateLayout.isVisible = true
            binding.openingBalanceDateEdittext.setOnClickListener {
                val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                val picker = materialDatePicker.build()
                picker.show(childFragmentManager, picker.toString())
                picker.addOnPositiveButtonClickListener { time ->
                    binding.openingBalanceDateEdittext.setText(DateTimeUtil.getCalToString(time.toString()))
                }
            }
            binding.virtualBalanceLayout.isVisible = true
            binding.openingBalanceLayout.isVisible = true
            binding.accountRoleSpinner.isVisible = true
            binding.currencyLayout.isVisible = true
        }
        if(accountType == "liabilities"){
            binding.currencyLayout.isVisible = true
            binding.liabilityTypeSpinner.isVisible = true
            binding.startAmountLayout.isVisible = true
            binding.startDateLayout.isVisible = true
            binding.startAmountText.isVisible = true
            binding.startDateEdittext.setOnClickListener {
                val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                val picker = materialDatePicker.build()
                picker.show(childFragmentManager, picker.toString())
                picker.addOnPositiveButtonClickListener { time ->
                    binding.startDateEdittext.setText(DateTimeUtil.getCalToString(time.toString()))
                }
            }
            binding.interestLayout.isVisible = true
            binding.interestPeriodSpinner.isVisible = true
        }
        binding.includeInNetWorthText.setOnClickListener {
            binding.includeInNetWorthCheck.performClick()
        }
        accountViewModel.isLoading.observe(viewLifecycleOwner) { loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        binding.noteEdittext.setOnClickListener {
            markdownViewModel.markdownText.postValue(binding.noteEdittext.getString())
            parentFragmentManager.commit {
                add(R.id.bigger_fragment_container, MarkdownFragment())
                addToBackStack(null)
            }
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            binding.noteEdittext.setText(markdownText)
        }
    }

    override fun submitData() {
        val netWorth = binding.includeInNetWorthCheck.isChecked
        val accountRoleSpinner: String? = if(binding.accountRoleSpinner.isVisible){
            when {
                binding.accountRoleSpinner.selectedItemPosition == 0 -> "defaultAsset"
                binding.accountRoleSpinner.selectedItemPosition == 1 -> "sharedAsset"
                binding.accountRoleSpinner.selectedItemPosition == 2 -> "savingAsset"
                binding.accountRoleSpinner.selectedItemPosition == 3 -> "ccAsset"
                else -> "cashWalletAsset "
            }
        } else {
            null
        }
        val iBanString = if (binding.ibanEdittext.isBlank()) {
            null
        } else {
            binding.ibanEdittext.getString()
        }
        val bicString = if (binding.bicEdittext.isBlank()) {
            null
        } else {
            binding.bicEdittext.getString()
        }
        val accountNumberString = if (binding.accountNumberEdittext.isBlank()) {
            null
        } else {
            binding.accountNumberEdittext.getString()
        }
        val openingBalanceValue: String? = if(binding.openingBalanceLayout.isVisible){
            binding.openingBalanceEdittext.getString()
        } else {
            null
        }
        val openingBalanceDate: String? = if(binding.openingBalanceDateLayout.isVisible){
            binding.openingBalanceDateEdittext.getString()
        } else {
            null
        }
        val virtualBalanceString: String? = if(binding.virtualBalanceLayout.isVisible){
            binding.virtualBalanceEdittext.getString()
        } else {
            null
        }
        val notesText = if (binding.noteEdittext.isBlank()) {
            null
        } else {
            binding.noteEdittext.getString()
        }
        val currencyToBeSubmitted = if(binding.currencyLayout.isVisible && accountViewModel.currency.isNotBlank()){
            accountViewModel.currency
        } else {
            null
        }
        val liabilityType: String? = if(binding.liabilityTypeSpinner.isVisible){
            when {
                binding.liabilityTypeSpinner.selectedItemPosition == 0 -> "debt"
                binding.liabilityTypeSpinner.selectedItemPosition == 1 -> "loan"
                else -> "mortgage"
            }
        } else {
            null
        }
        val startAmountOfDebt: String? = if(binding.startAmountLayout.isVisible){
            binding.startAmountEdittext.getString()
        } else {
            null
        }
        val startAmountDay: String? = if(binding.startDateLayout.isVisible){
            binding.startDateEdittext.getString()
        } else {
            null
        }
        val interestAmount: String? = if(binding.interestLayout.isVisible){
            binding.interestEdittext.getString()
        } else {
            null
        }
        val interestPeriod: String? = if(binding.interestPeriodSpinner.isVisible){
            when {
                binding.interestPeriodSpinner.selectedItemPosition == 0 -> "daily"
                binding.interestPeriodSpinner.selectedItemPosition == 1 -> "monthly"
                else -> "yearly"
            }
        } else {
            null
        }
        if(accountId == 0L){
            addAccount(binding.descriptionEdittext.getString(), accountType, currencyToBeSubmitted,
                    iBanString, bicString, accountNumberString, openingBalanceValue, openingBalanceDate,
                    accountRoleSpinner, virtualBalanceString, netWorth, notesText, liabilityType, startAmountOfDebt,
                    startAmountDay, interestAmount, interestPeriod)
        } else {
            updateAccount(binding.descriptionEdittext.getString(), accountType, currencyToBeSubmitted,
                    iBanString, bicString, accountNumberString, openingBalanceValue, openingBalanceDate,
                    accountRoleSpinner, virtualBalanceString, netWorth, notesText, liabilityType, startAmountOfDebt,
                    startAmountDay, interestAmount, interestPeriod)
        }
    }

    private fun updateAccount(accountName: String, accountType: String,
                              currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                              openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                              virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                              liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?){
        accountViewModel.updateAccount(accountId,accountName, accountType, currencyCode,
                iban, bic, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod).observe(viewLifecycleOwner){ response ->
            if(response.first){
                toastSuccess("Account saved")
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun addAccount(accountName: String, accountType: String,
                           currencyCode: String?, iban: String?, bic: String?, accountNumber: String?,
                           openingBalance: String?, openingBalanceDate: String?, accountRole: String?,
                           virtualBalance: String?, includeInNetWorth: Boolean, notes: String?, liabilityType: String?,
                           liabilityAmount: String?, liabilityStartDate: String?, interest: String?, interestPeriod: String?){
        accountViewModel.addAccount(accountName, accountType, currencyCode,
                iban, bic, accountNumber, openingBalance, openingBalanceDate,
                accountRole, virtualBalance, includeInNetWorth, notes, liabilityType, liabilityAmount,
                liabilityStartDate, interest, interestPeriod, attachmentItemAdapter).observe(viewLifecycleOwner){ response ->
            if(response.first){
                handleBack()
                toastSuccess(response.second)
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            } else {
                toastInfo(response.second)
            }
        }
    }

    private fun updateData(){
        if(accountId != 0L){
            accountViewModel.getAccountById(accountId).observe(viewLifecycleOwner) { accountData ->
                val accountAttributes = accountData.accountAttributes
                binding.descriptionEdittext.setText(accountAttributes.name)
                binding.currencyEdittext.setText(accountAttributes.currency_code + " (" + accountAttributes.currency_symbol + " )")
                accountViewModel.currency = accountAttributes.currency_code ?: ""
                val liabilityType = accountAttributes.liability_type
                if(liabilityType != null){
                    when (liabilityType) {
                        "debt" -> binding.liabilityTypeSpinner.setSelection(0)
                        "loan" -> binding.liabilityTypeSpinner.setSelection(1)
                        "mortgage" -> binding.liabilityTypeSpinner.setSelection(2)
                    }
                }
                binding.startAmountEdittext.setText(accountAttributes.liability_amount)
                binding.startDateEdittext.setText(accountAttributes.liability_start_date)
                binding.interestEdittext.setText(accountAttributes.interest)
                val interestPeriod = accountAttributes.interest_period
                if(interestPeriod != null){
                    when(interestPeriod){
                        "daily" -> binding.interestPeriodSpinner.setSelection(0)
                        "monthly" -> binding.interestPeriodSpinner.setSelection(1)
                        "yearly" -> binding.interestPeriodSpinner.setSelection(2)
                    }
                }
                binding.ibanEdittext.setText(accountAttributes.iban)
                binding.bicEdittext.setText(accountAttributes.bic)
                binding.accountNumberEdittext.setText(accountAttributes.account_number)
                if(accountAttributes.include_net_worth){
                    binding.includeInNetWorthCheck.performClick()
                }
                binding.openingBalanceEdittext.setText(accountAttributes.opening_balance.toString())
                binding.openingBalanceDateEdittext.setText(accountAttributes.opening_balance_date)
                val accountRole = accountAttributes.account_role
                if(accountRole != null){
                    when(accountRole){
                        "defaultAsset" -> binding.accountRoleSpinner.setSelection(0)
                        "sharedAsset" -> binding.accountRoleSpinner.setSelection(1)
                        "savingAsset" -> binding.accountRoleSpinner.setSelection(2)
                        "ccAsset" -> binding.accountRoleSpinner.setSelection(3)
                        "cashWalletAsset" -> binding.accountRoleSpinner.setSelection(4)
                    }
                }
                binding.virtualBalanceEdittext.setText(accountAttributes.virtual_balance.toString())
                binding.noteEdittext.setText(accountAttributes.notes)
            }
            displayAttachment()
        } else {
            showHelpText()
        }
    }

    private fun displayAttachment(){
        accountViewModel.accountAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        false, { data: AttachmentData ->
                    AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.are_you_sure))
                            .setPositiveButton(android.R.string.ok){ _, _ ->
                                accountViewModel.deleteAttachment(data).observe(viewLifecycleOwner){ isSuccessful ->
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

    private fun showHelpText(){
        FancyShowCaseQueue()
                .add(showCase(R.string.add_account_currency_help_text,
                        "addAccountCurrencyCaseView", binding.currencyLayout)).show()
    }

    private fun showHiddenHelpText(){
        if(binding.ibanLayout.isVisible && binding.expansionLayout.isExpanded) {
            val queue = FancyShowCaseQueue()
            queue.add(showCase(R.string.iban_help_text, "ibanCaseView",
                    binding.ibanLayout, true))
            if(binding.openingBalanceLayout.isVisible){
                queue.add(showCase(R.string.opening_balance_help_text, "openingBalanceCaseView",
                        binding.openingBalanceLayout))
            }
            if(binding.virtualBalanceLayout.isVisible) {
                queue.add(showCase(R.string.virtual_balance_help_text, "virtualBalanceCaseView",
                        binding.virtualBalanceLayout))
            }
            queue.show()
        }
    }
    
    private fun handleBack() {
        unReveal(binding.addAccountLayout)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAddAccountBinding = null
        markdownViewModel.markdownText.postValue("")
    }
}