package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import kotlin.collections.ArrayList

class TransactionFragmentV2: BaseTransactionFragment(){

    private var currentDate = DateTimeUtil.getTodayDate()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runLayoutAnimation(recycler_view)
        getDate()
        pullToRefresh()
    }

    private fun loadTransaction(){
        swipeContainer.isRefreshing = true
        dataAdapter.clear()
        transactionViewModel.getTransactionList(currentDate, currentDate, transactionType).observe(this) {
            dataAdapter = ArrayList(it)
            displayResults()
        }
    }

    override fun itemClicked(data: Transactions){
        fragment_transaction_rootview.isVisible = false
        requireFragmentManager().commit {
            add(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun setupFab(){
        addTransactionFab.apply {
            translationX = (6 * 56).toFloat()
            animate().apply {
                translationX(0f)
                interpolator = OvershootInterpolator(3f)
                startDelay = 300
                duration = 400
                start()
            }
            setOnClickListener {
                addTransactionFab.isClickable = false
                val addTransaction = AddTransactionFragment()
                addTransaction.arguments = bundleOf("revealX" to addTransactionFab.width / 2,
                        "revealY" to addTransactionFab.height / 2, "transactionType" to transactionType,
                        "SHOULD_HIDE" to true)
                requireFragmentManager().commit {
                    replace(R.id.bigger_fragment_container, addTransaction)
                    addToBackStack(null)
                }
                addTransactionFab.isClickable = true
                addTransactionFab.isVisible = false
                fragmentContainer.isVisible = false
            }
            setImageDrawable(IconicsDrawable(requireContext())
                    .icon(GoogleMaterial.Icon.gmd_add)
                    .colorRes(R.color.md_pink_200)
                    .sizeDp(16))
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            loadTransaction()
        }
    }

    private fun getDate(){
        loadTransaction()
        transaction_calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val correctDate = if(dayOfMonth in 1..9){
                "0$dayOfMonth"
            } else {
                dayOfMonth.toString()
            }
            val correctMonth: String = if(month in 0..8){
                "0" + ((1 + month).toString())
            } else {
                (month + 1).toString()
            }
            // the start of month is 0?! start calendar view is stupid really???!!
            currentDate = "$year-$correctMonth-$correctDate"
            loadTransaction()
        }
    }
}