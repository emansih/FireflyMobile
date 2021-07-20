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

package xyz.hisname.fireflyiii.ui.transaction.details

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.DetailsCardBinding
import xyz.hisname.fireflyiii.databinding.FragmentTransactionDetailsBinding
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.details.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.ui.tags.TagDetailsFragment
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionPager
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.openFile


class TransactionDetailsFragment: BaseDetailFragment() {

    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId", 0) ?: 0 }
    private val transactionDetailsViewModel by lazy { getImprovedViewModel(TransactionDetailsViewModel::class.java) }
    private var transactionList: MutableList<DetailModel> = ArrayList()
    private var metaDataList: MutableList<DetailModel> = arrayListOf()
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private var sourceAccountId = 0L
    private var destinationAccountId = 0L
    private var transactionInfo = ""
    private var transactionDescription  = ""
    private var transactionDate = ""
    private var transactionCategory = ""
    private var transactionBudget = ""
    private var transactionAmount = ""
    private lateinit var chipTags: Chip

    private var fragmentTransactionDetailsBinding: FragmentTransactionDetailsBinding? = null
    private val binding get() = fragmentTransactionDetailsBinding!!
    private var detailsCardBinding: DetailsCardBinding? = null
    private val cardBinding get() = detailsCardBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentTransactionDetailsBinding = FragmentTransactionDetailsBinding.inflate(inflater, container, false)
        detailsCardBinding = binding.transactionInfoCard
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        setBottomNavigation()
        transactionDetailsViewModel.isLoading.observe(viewLifecycleOwner){ loading ->
            if(loading){
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun getData(){
        transactionDetailsViewModel.getTransactionByJournalId(transactionJournalId).observe(viewLifecycleOwner){ transactionData ->
            setTransactionInfo(transactionData)
            setMetaInfo(transactionData)
            setNotes(transactionData.notes)
        }
    }

    private fun setTransactionInfo(transactionData: Transactions){
        transactionList.clear()
        val details = transactionData
        val model = arrayListOf(DetailModel(requireContext().getString(R.string.transactionType), details.transactionType),
                DetailModel(resources.getString(R.string.description), details.description),
                DetailModel(resources.getString(R.string.source_account), details.source_name),
                DetailModel(resources.getString(R.string.destination_account), details.destination_name),
                DetailModel(resources.getString(R.string.date), DateTimeUtil.convertIso8601ToHumanDate(details.date)),
                DetailModel(resources.getString(R.string.time), DateTimeUtil.convertIso8601ToHumanTime(details.date)))
        if(details.foreign_amount != null && details.foreign_currency_symbol != null){
            transactionList.add(DetailModel(resources.getString(R.string.amount),
                    details.currency_symbol + details.amount.toString() + " (" + details.foreign_currency_symbol
                            + details.foreign_amount + ")"))
            transactionAmount = details.currency_symbol + details.amount.toString() + " (" + details.foreign_currency_symbol +
                    details.foreign_amount + ")"
        } else {
            transactionAmount = details.currency_symbol + details.amount
            transactionList.add(DetailModel(resources.getString(R.string.amount),details.currency_symbol + details.amount.toString()))
        }
        transactionDescription = details.description
        sourceAccountId = details.source_id ?: 0L
        destinationAccountId = details.destination_id
        transactionInfo = details.transactionType
        transactionDate = details.date.toString()
        downloadAttachment()
        transactionList.addAll(model)
        cardBinding.infoText.text = getString(R.string.transaction_information)
        cardBinding.detailsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cardBinding.detailsRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        cardBinding.detailsRecyclerView.adapter = BaseDetailRecyclerAdapter(transactionList){ position: Int -> setTransactionInfoClick(position)}
    }

    private fun setMetaInfo(details: Transactions){
        metaDataList.clear()
        transactionCategory = details.category_name ?: ""
        transactionBudget = details.budget_name ?: ""
        val model = arrayListOf(DetailModel(resources.getString(R.string.categories),
                transactionCategory), DetailModel(resources.getString(R.string.budget), transactionBudget))
        metaDataList.addAll(model)
        val tagsInTransaction = details.tags
        if(tagsInTransaction.isNotEmpty()){
            binding.transactionTags.setChipSpacing(16)
            tagsInTransaction.forEach { nameOfTag ->
                chipTags = Chip(requireContext())
                chipTags.apply {
                    text = nameOfTag
                    setOnClickListener {
                        parentFragmentManager.commit {
                            val tagDetails = TagDetailsFragment()
                            tagDetails.arguments = bundleOf("revealX" to extendedFab.width / 2,
                                    "revealY" to extendedFab.height / 2, "tagName" to nameOfTag)
                            addToBackStack(null)
                            replace(R.id.fragment_container, tagDetails)
                        }
                    }
                }
                binding.transactionTags.addView(chipTags)
            }
            binding.transactionTagsCard.isVisible = true
        }
        binding.metaInformation.layoutManager = LinearLayoutManager(requireContext())
        binding.metaInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.metaInformation.adapter = BaseDetailRecyclerAdapter(metaDataList){  }
    }

    private fun setTransactionInfoClick(position: Int){
        when(position){
            3 -> {
                parentFragmentManager.commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to sourceAccountId)
                    })
                    addToBackStack(null)
                }
            }
            4 -> {
                parentFragmentManager.commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to destinationAccountId)
                    })
                    addToBackStack(null)
                }
            }
        }
    }

    private fun downloadAttachment(){
        transactionDetailsViewModel.transactionAttachment.observe(viewLifecycleOwner){ attachment ->
            if(attachment.isNotEmpty()){
                binding.attachmentInformationCard.isVisible = true
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentInformation.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentInformation.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                    true, { data: AttachmentData ->
                    setDownloadClickListener(data, attachmentDataAdapter)
                    }){ another: Int -> }
            }
        }
    }

    private fun setNotes(notes: String?){
        if(notes.isNullOrEmpty()){
            binding.notesCard.isGone = true
        } else {
            binding.notesText.text = notes.toMarkDown()
        }
    }

    private fun setDownloadClickListener(attachmentData: AttachmentData, attachmentAdapter: ArrayList<AttachmentData>){
        transactionDetailsViewModel.downloadAttachment(attachmentData).observe(viewLifecycleOwner){ downloadedFile ->
            binding.attachmentInformation.adapter = AttachmentRecyclerAdapter(attachmentAdapter,
                    true, { data: AttachmentData ->
                setDownloadClickListener(data, attachmentDataAdapter)
            }){ another: Int -> }
            startActivity(requireContext().openFile(downloadedFile))
        }
    }

    private fun setBottomNavigation(){
        val shapeDrawable: MaterialShapeDrawable = binding.transactionDetailsBottomNavView.background as MaterialShapeDrawable
        shapeDrawable.shapeAppearanceModel = shapeDrawable.shapeAppearanceModel
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 16f)
            .build()

        val editIcon = IconicsDrawable(requireContext()).apply { icon = GoogleMaterial.Icon.gmd_edit }
        val deleteIcon = IconicsDrawable(requireContext()).apply { icon = GoogleMaterial.Icon.gmd_delete }
        val duplicateIcon = IconicsDrawable(requireContext()).apply { icon = GoogleMaterial.Icon.gmd_file_copy }

        if(isDarkMode()) {
            editIcon.colorRes = R.color.md_white_1000
            deleteIcon.colorRes = R.color.md_white_1000
            duplicateIcon.colorRes = R.color.md_white_1000
        }

        binding.transactionDetailsBottomNavView.itemIconTintList = null
        binding.transactionDetailsBottomNavView.menu
            .add(Menu.NONE, 1, Menu.NONE, "Edit").icon = editIcon
        binding.transactionDetailsBottomNavView.menu
            .add(Menu.NONE, 2, Menu.NONE, "Delete").icon = deleteIcon
        binding.transactionDetailsBottomNavView.menu
            .add(Menu.NONE, 3, Menu.NONE, "Duplicate")
            .icon = duplicateIcon
        binding.transactionDetailsBottomNavView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                1 -> {
                    editItem()
                }
                2 -> {
                    deleteItem()
                }
                3 -> {
                    duplicateTransaction()
                }
            }
            true
        }
    }

    private fun duplicateTransaction(){
        val attachmentUri = arrayListOf<Uri>()
        attachmentDataAdapter.forEach {  attachmentData ->
            attachmentUri.add(attachmentData.attachmentAttributes.download_uri)
        }
        transactionDetailsViewModel.duplicationTransactionByJournalId(transactionJournalId,
            attachmentUri).observe(viewLifecycleOwner){ message ->
            toastInfo(message)
        }

    }

    override fun deleteItem() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_transaction_title, transactionDescription))
                .setMessage(resources.getString(R.string.delete_transaction_message, transactionDescription))
                .setIcon(IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_trash
                    sizeDp = 24
                    colorRes = R.color.md_green_600
                })
                .setPositiveButton(R.string.delete_permanently) { _, _ ->
                    transactionDetailsViewModel.deleteTransaction(transactionJournalId).observe(viewLifecycleOwner) {
                        if (it) {
                            toastSuccess(resources.getString(R.string.transaction_deleted))
                            handleBack()
                        } else {
                            toastOffline(getString(R.string.data_will_be_deleted_later, transactionDescription),
                                    Toast.LENGTH_LONG)
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
    }

    override fun editItem() {
        val addTransaction = AddTransactionPager().apply {
            arguments = bundleOf("transactionJournalId" to transactionJournalId, "SHOULD_HIDE" to true,
                    "transactionType" to convertString(transactionInfo))
        }
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, addTransaction)
            addToBackStack(null)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        android.R.id.home -> consume {
            parentFragmentManager.popBackStack()
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun handleBack() {
        parentFragmentManager.popBackStack()
        val mainToolbar = requireActivity().findViewById<Toolbar>(R.id.activity_toolbar)
        mainToolbar.title = convertString(transactionInfo)
    }

    private fun convertString(type: String) = type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase()

    override fun onDestroy() {
        super.onDestroy()
        detailsCardBinding = null
        fragmentTransactionDetailsBinding = null
    }

}