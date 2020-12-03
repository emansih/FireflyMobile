package xyz.hisname.fireflyiii.ui.transaction.details

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.details_card.*
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import kotlinx.android.synthetic.main.fragment_transaction_details.attachment_information
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.attachment.AttachmentViewModel
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.tags.TagDetailsFragment
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.openFile


class TransactionDetailsFragment: BaseFragment() {

    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId", 0) ?: 0 }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_transaction_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        extendedFab.isVisible = false
    }

    private fun retrieveData(){
        transactionViewModel.getTransactionByJournalId(transactionJournalId).observe(viewLifecycleOwner){ transactionData ->
            setTransactionInfo(transactionData)
            setMetaInfo(transactionData)
        }
    }

    private fun setTransactionInfo(transactionData: MutableList<Transactions>){
        transactionList.clear()
        val details = transactionData[0]
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
        transactionDescription = details.description ?: ""
        sourceAccountId = details.source_id ?: 0L
        destinationAccountId = details.destination_id
        transactionInfo = details.transactionType ?: ""
        transactionDate = details.date.toString()
        downloadAttachment(details.transaction_journal_id)
        transactionList.addAll(model)
        info_text.text = getString(R.string.transaction_information)
        detailsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        detailsRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        detailsRecyclerView.adapter = BaseDetailRecyclerAdapter(transactionList){ position: Int -> setTransactionInfoClick(position)}
    }

    private fun setMetaInfo(transactionData: MutableList<Transactions>){
        metaDataList.clear()
        val details = transactionData[0]
        transactionCategory = details.category_name ?: ""
        transactionBudget = details.budget_name ?: ""
        val model = arrayListOf(DetailModel(resources.getString(R.string.categories),
                transactionCategory), DetailModel(resources.getString(R.string.budget), transactionBudget))
        metaDataList.addAll(model)
        val tagsInTransaction = details.tags
        if(tagsInTransaction.isNotEmpty()){
            transaction_tags.setChipSpacing(16)
            tagsInTransaction.forEachIndexed { _, nameOfTag ->
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
                transaction_tags.addView(chipTags)
            }
            transaction_tags_card.isVisible = true
        }
        meta_information.layoutManager = LinearLayoutManager(requireContext())
        meta_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        meta_information.adapter = BaseDetailRecyclerAdapter(metaDataList){ position: Int -> setMetaInfoClick(position)}
    }

    private fun setMetaInfoClick(position: Int){
        when(position) {
            0 -> {
                if (transactionCategory.isNotEmpty()) {
                    AlertDialog.Builder(requireContext())
                            .setTitle(resources.getString(R.string.categories))
                            .setMessage(transactionCategory)
                            .show()
                }
            }
            1 -> {
                if (transactionBudget.isNotEmpty()) {
                    AlertDialog.Builder(requireContext())
                            .setTitle(resources.getString(R.string.budget))
                            .setMessage(transactionBudget)
                            .show()
                }
            }
        }
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

    private fun downloadAttachment(journalId: Long){
        transactionViewModel.getTransactionAttachment(journalId).observe(viewLifecycleOwner) { attachment ->
            transactionViewModel.isLoading.observe(viewLifecycleOwner){ loading ->
                if (!loading && attachment.isNotEmpty()) {
                    attachment_information_card.isVisible = true
                    attachmentDataAdapter = ArrayList(attachment)
                    attachment_information.layoutManager = LinearLayoutManager(requireContext())
                    attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                    attachment_information.adapter = TransactionAttachmentRecyclerAdapter(attachmentDataAdapter,
                            true, { data: AttachmentData ->
                        setDownloadClickListener(data, attachmentDataAdapter)
                    }){ another: Int -> }
                }
            }
        }
    }

    private fun setDownloadClickListener(attachmentData: AttachmentData, attachmentAdapter: ArrayList<AttachmentData>){
        ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
        val attachmentViewModel = getViewModel(AttachmentViewModel::class.java)
        attachmentViewModel.downloadAttachment(attachmentData).observe(viewLifecycleOwner) { downloadedFile ->
            attachmentViewModel.isDownloaded.observe(viewLifecycleOwner) { isLoading ->
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                if (!isLoading) {
                    toastError("There was an issue downloading " + attachmentData.attachmentAttributes?.filename)
                } else {
                    // "Refresh" the icon. From downloading to open file
                    attachment_information.adapter = TransactionAttachmentRecyclerAdapter(attachmentAdapter,
                            true, { data: AttachmentData ->
                        setDownloadClickListener(data, attachmentDataAdapter)
                    }){ another: Int -> }
                    startActivity(requireContext().openFile(downloadedFile))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.transaction_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

    override fun onResume() {
        super.onResume()
        extendedFab.isVisible = false
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        android.R.id.home -> consume {
            parentFragmentManager.popBackStack()
        }
        R.id.menu_item_delete -> consume {
            AlertDialog.Builder(requireContext())
                    .setTitle(resources.getString(R.string.delete_account_title, transactionDescription))
                    .setMessage(resources.getString(R.string.delete_transaction_message, transactionDescription))
                    .setIcon(IconicsDrawable(requireContext()).apply {
                        icon = FontAwesome.Icon.faw_trash
                        sizeDp = 24
                        colorRes = R.color.md_green_600
                    })
                    .setPositiveButton(R.string.delete_permanently) { _, _ ->
                        ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
                        transactionViewModel.deleteTransaction(transactionJournalId).observe(this) {
                            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
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
        R.id.menu_item_edit -> consume {
            val addTransaction = AddTransactionFragment().apply {
                arguments = bundleOf("transactionJournalId" to transactionJournalId, "SHOULD_HIDE" to true,
                        "transactionType" to convertString(transactionInfo))
            }
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, addTransaction)
                addToBackStack(null)
            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
        val mainToolbar = requireActivity().findViewById<Toolbar>(R.id.activity_toolbar)
        mainToolbar.title = convertString(transactionInfo)
    }

    private fun convertString(type: String) = type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase()

}