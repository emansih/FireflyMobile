package xyz.hisname.fireflyiii.ui.transaction.details

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.attachment.AttachmentViewModel
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.account.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.tags.TagDetailsFragment
import xyz.hisname.fireflyiii.ui.transaction.DeleteTransactionDialog
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.collections.ArrayList

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
    private lateinit var attachmentData: AttachmentData

    companion object {
        private const val STORAGE_REQUEST_CODE = 1337
    }

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
        fab.isVisible = false
    }

    private fun retrieveData(){
        transactionViewModel.getTransactionByJournalId(transactionJournalId).observe(this){ transactionData ->
            setTransactionInfo(transactionData)
            setMetaInfo(transactionData)
        }
    }

    private fun setTransactionInfo(transactionData: MutableList<Transactions>){
        transactionList.clear()
        val details = transactionData[0]
        val model = arrayListOf(DetailModel("Type", details.transactionType),
                DetailModel(resources.getString(R.string.description), details.description),
                DetailModel(resources.getString(R.string.source_account), details.source_name),
                DetailModel(resources.getString(R.string.destination_account), details.destination_name),
                DetailModel(resources.getString(R.string.date), details.date.toString()))
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
        transaction_info.layoutManager = LinearLayoutManager(requireContext())
        transaction_info.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        transaction_info.adapter = TransactionDetailsRecyclerAdapter(transactionList){ position: Int -> setTransactionInfoClick(position)}
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
                    chipIcon = IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_tag)
                            .colorInt(R.color.md_green_400)
                    addColor()
                    setOnClickListener {
                        requireFragmentManager().commit {
                            val tagDetails = TagDetailsFragment()
                            tagDetails.arguments = bundleOf("revealX" to fab.width / 2,
                                    "revealY" to fab.height / 2, "tagName" to nameOfTag)
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
        meta_information.adapter = TransactionDetailsRecyclerAdapter(metaDataList){ position: Int -> setMetaInfoClick(position)}
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
            0 -> {
                AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.amount))
                        .setMessage(transactionAmount)
                        .show()
            }
            2 ->{
               AlertDialog.Builder(requireContext())
                       .setTitle(resources.getString(R.string.description))
                       .setMessage(transactionDescription)
                       .show()
            }
            3 -> {
                requireFragmentManager().commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to sourceAccountId)
                    })
                    addToBackStack(null)
                }
            }
            4 -> {
                requireFragmentManager().commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to destinationAccountId)
                    })
                    addToBackStack(null)
                }
            }
            5 -> {
                AlertDialog.Builder(requireContext())
                        .setTitle(resources.getString(R.string.date))
                        .setMessage(transactionDate)
                        .show()
            }
        }
    }

    private fun downloadAttachment(journalId: Long){
        transactionViewModel.getTransactionAttachment(journalId).observe(this) { attachment ->
            transactionViewModel.isLoading.observe(this){ loading ->
                if (!loading && attachment.isNotEmpty()) {
                    attachment_information_card.isVisible = true
                    attachmentDataAdapter = ArrayList(attachment)
                    attachment_information.layoutManager = LinearLayoutManager(requireContext())
                    attachment_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                    attachment_information.adapter = TransactionAttachmentRecyclerAdapter(ArrayList(attachment)) { data: AttachmentData ->
                        attachmentData = data
                        setDownloadClickListener(data)
                    }
                }
            }
        }
    }

    private fun setDownloadClickListener(attachmentData: AttachmentData){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else {
            val attachmentViewModel = getViewModel(AttachmentViewModel::class.java)
            attachmentViewModel.downloadAttachment(attachmentData).observe(this) { isDownloaded ->
                attachmentViewModel.isLoading.observe(this) { isLoading ->
                    if (!isDownloaded && !isLoading) {
                        toastError("There was an issue downloading " + attachmentData.attachmentAttributes?.filename)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.size == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setDownloadClickListener(attachmentData)
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
        fab.isVisible = false
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        android.R.id.home -> consume {
            requireParentFragment().parentFragmentManager.popBackStack()
        }
        R.id.menu_item_delete -> consume {
            requireFragmentManager().commit {
                add(DeleteTransactionDialog().apply {
                    arguments = bundleOf("transactionJournalId" to transactionJournalId, "transactionDescription" to transactionDescription)
                }, "")
            }
        }
        R.id.menu_item_edit -> consume {
            val addTransaction = AddTransactionFragment().apply {
                arguments = bundleOf("transactionJournalId" to transactionJournalId, "SHOULD_HIDE" to true,
                        "transactionType" to transactionInfo)
            }
            fragmentContainer.isVisible = false
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, addTransaction)
            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun handleBack() {
        requireParentFragment().parentFragmentManager.popBackStack()
        val v2Layout = requireActivity().findViewById<CoordinatorLayout>(R.id.fragment_transaction_rootview)
        if(v2Layout != null){
            v2Layout.isVisible = true
            val mainToolbar = requireActivity().findViewById<Toolbar>(R.id.activity_toolbar)
            mainToolbar.title = convertString(transactionInfo)
        }
    }

    private fun convertString(type: String) = type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase()

}