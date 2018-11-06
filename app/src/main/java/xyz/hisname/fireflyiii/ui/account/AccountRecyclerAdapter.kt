package xyz.hisname.fireflyiii.ui.account

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.account_list_item.view.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.ui.base.DiffUtilAdapter
import xyz.hisname.fireflyiii.util.extension.inflate

class AccountRecyclerAdapter(private val items: MutableList<AccountData>, private val clickListener:(AccountData) -> Unit):
        DiffUtilAdapter<AccountData, AccountRecyclerAdapter.AccountViewHolder>(){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        context = parent.context
        return AccountViewHolder(parent.inflate(R.layout.account_list_item))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) = holder.bind(items[position],clickListener)


    inner class AccountViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(data: AccountData, clickListener: (AccountData) -> Unit){
            val accountData = data.accountAttributes
            var currencySymbol = ""
            if(accountData?.currency_symbol != null){
                currencySymbol = accountData.currency_symbol
            }
            itemView.accountNameText.text = accountData?.name
            itemView.accountAmountText.text = currencySymbol + " " +accountData?.current_balance.toString()
            itemView.setOnClickListener { clickListener(data) }
        }
    }
}