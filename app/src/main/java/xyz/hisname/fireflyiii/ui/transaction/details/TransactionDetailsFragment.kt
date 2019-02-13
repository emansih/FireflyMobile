package xyz.hisname.fireflyiii.ui.transaction.details

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.ui.account.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.DeleteTransactionDialog
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.util.extension.consume
import xyz.hisname.fireflyiii.util.extension.create

class TransactionDetailsFragment: BaseFragment() {

    private val transactionId by lazy { arguments?.getLong("transactionId", 0) ?: 0 }
    private var transactionList: MutableList<DetailModel> = ArrayList()
    private var metaDataList: MutableList<DetailModel> = arrayListOf()
    private var sourceAccountId = 0L
    private var destinationAccountId = 0L
    private var transactionInfo = ""
    private var transactionDescription  = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_transaction_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransactionInfo()
        setMetaInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        fab.isVisible = false
    }

    private fun setTransactionInfo(){
        transactionList.clear()
        transactionViewModel.getTransactionById(transactionId).observe(this, Observer {
            val details = it[0].transactionAttributes
            val model = arrayListOf(DetailModel("Type", details?.transactionType),
                    DetailModel(resources.getString(R.string.description), details?.description),
                    DetailModel(resources.getString(R.string.source_account), details?.source_name),
                    DetailModel(resources.getString(R.string.destination_account), details?.destination_name),
                    DetailModel(resources.getString(R.string.amount),details?.currency_symbol + details?.amount.toString()),
                    DetailModel(resources.getString(R.string.date), details?.date.toString()))
            transactionDescription = details?.description ?: ""
            sourceAccountId = details?.source_id?.toLong() ?: 0L
            destinationAccountId = details?.destination_id?.toLong() ?: 0L
            transactionInfo = details?.transactionType ?: ""
            transactionList.addAll(model)
            transaction_info.layoutManager = LinearLayoutManager(requireContext())
            transaction_info.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            transaction_info.adapter = TransactionDetailsRecyclerAdapter(transactionList){ position: Int -> setTransactionInfoClick(position)}
        })
    }

    private fun setMetaInfo(){
        metaDataList.clear()
        transactionViewModel.getTransactionById(transactionId).observe(this, Observer {
            val details = it[0].transactionAttributes
            val model = arrayListOf(DetailModel(resources.getString(R.string.categories),
                    details?.category_name), DetailModel(resources.getString(R.string.budget), details?.budget_name))
            metaDataList.addAll(model)
            meta_information.layoutManager = LinearLayoutManager(requireContext())
            meta_information.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            meta_information.adapter = TransactionDetailsRecyclerAdapter(metaDataList){ position: Int -> }
        })
    }

    private fun setTransactionInfoClick(position: Int){
        when(position){
            2 -> {
                requireFragmentManager().commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to sourceAccountId)
                    })
                    addToBackStack(null)
                }
            }
            3 -> {
                requireFragmentManager().commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to destinationAccountId)
                    })
                    addToBackStack(null)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.transaction_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        when(transactionInfo) {
            "Withdrawal" -> {
                menu.removeItem(R.id.menu_item_withdraw)
            }
            "Deposit" -> {
                menu.removeItem(R.id.menu_item_deposit)
            }
            "Transfer" -> {
                menu.removeItem(R.id.menu_item_transfer)
            }
        }
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

    override fun onStop() {
        super.onStop()
        fab.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        android.R.id.home -> consume {
            requireFragmentManager().popBackStack()
        }
        R.id.menu_item_delete -> consume {
            requireFragmentManager().commit {
                add(DeleteTransactionDialog().apply {
                    arguments = bundleOf("transactionId" to transactionId, "transactionDescription" to transactionDescription)
                }, "")
            }
        }
        R.id.menu_item_edit -> consume {
            val addTransaction = AddTransactionFragment().apply {
                arguments = bundleOf("transactionId" to transactionId)
            }
            requireFragmentManager().commit {
                replace(R.id.bigger_fragment_container, addTransaction)
            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun handleBack() {
        requireFragmentManager().popBackStack()
    }
}