package xyz.hisname.fireflyiii.ui

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.cursoradapter.widget.CursorAdapter
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.account.details.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.bills.details.BillDetailsFragment
import xyz.hisname.fireflyiii.ui.budget.AddBudgetFragment
import xyz.hisname.fireflyiii.ui.categories.CategoryDetailsFragment
import xyz.hisname.fireflyiii.ui.currency.AddCurrencyFragment
import xyz.hisname.fireflyiii.ui.piggybank.details.PiggyDetailFragment
import xyz.hisname.fireflyiii.ui.tags.TagDetailsFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.extension.hideKeyboard

class SearchAdapter(private val activity: Activity, cursor: Cursor,
                    private val supportManager: FragmentManager): CursorAdapter(activity, cursor, false) {

    private lateinit var searchedItem: TextView
    private lateinit var searchedItemType: TextView
    private lateinit var searchedLayout: ConstraintLayout

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.search_items, parent, false)
        searchedItem = view.findViewById(R.id.searchedItem)
        searchedItemType = view.findViewById(R.id.searchedItemType)
        searchedLayout = view.findViewById(R.id.searchLayout)
        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val searchedId = cursor.getLong(0)
        val searchedResult = cursor.getString(1)
        val searchedResultType = cursor.getString(2)
        val searchedResultCurrency = cursor.getString(3)
        val parsedCurrency = if(searchedResultCurrency.isNotBlank()){
            "($searchedResultCurrency)"
        } else {
            ""
        }
        searchedItem.text = "$searchedResult $parsedCurrency"
        searchedItemType.text = searchedResultType
        searchedLayout.setOnClickListener {
            activity.hideKeyboard()
            if(searchedResultType.contains("withdrawal") || searchedResultType.contains("deposit")
                || searchedResultType.contains("transfer")){
                supportManager.commit {
                    replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                        arguments = bundleOf("transactionId" to searchedId)
                    })
                    addToBackStack(null)
                }
            } else if(searchedResultType.contains("Budget")){
                supportManager.commit {
                    replace(R.id.bigger_fragment_container, AddBudgetFragment().apply {
                        arguments = bundleOf("budgetId" to searchedId, "currencySymbol" to searchedResultCurrency)
                    })
                    addToBackStack(null)
                }
            } else if(searchedResultType.contains("Category")){
                supportManager.commit {
                    replace(R.id.fragment_container, CategoryDetailsFragment().apply {
                        arguments = bundleOf("categoryId" to searchedId)
                    })
                    addToBackStack(null)
                }
            } else if(searchedResultType.contains("Piggy Bank")){
                supportManager.commit {
                    replace(R.id.fragment_container, PiggyDetailFragment().apply {
                        arguments = bundleOf("piggyId" to searchedId)
                    })
                    addToBackStack(null)
                }
            } else if(searchedResultType.contains("Tags")){
                supportManager.commit {
                    replace(R.id.fragment_container, TagDetailsFragment().apply {
                        arguments = bundleOf( "tagId" to searchedId)
                    })
                    addToBackStack(null)
                }
            } else if(searchedResultType.contains("Currency")){
                supportManager.commit {
                    replace(R.id.bigger_fragment_container, AddCurrencyFragment().apply {
                        arguments = bundleOf("currencyId" to searchedId)
                    })
                    addToBackStack(null)
                }
            } else if(searchedResultType.contains("Bills")){
                supportManager.commit {
                    replace(R.id.fragment_container, BillDetailsFragment().apply {
                        arguments = bundleOf("billId" to searchedId)
                    })
                    addToBackStack(null)
                }
            } else if(searchedResultType.contains("asset") ||
                searchedResultType.contains("expense") ||
                searchedResultType.contains("revenue") ||
                searchedResultType.contains("liability")){
                supportManager.commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to searchedId)
                    })
                    addToBackStack(null)
                }
            }
        }
    }
}