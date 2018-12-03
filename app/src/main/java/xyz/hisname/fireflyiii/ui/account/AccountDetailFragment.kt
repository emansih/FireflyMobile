package xyz.hisname.fireflyiii.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_account_detail.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.BaseDetailModel
import xyz.hisname.fireflyiii.repository.models.accounts.AccountAttributes
import xyz.hisname.fireflyiii.repository.account.AccountsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.util.extension.*

class AccountDetailFragment: BaseDetailFragment() {

    private val accountViewModel by lazy { getViewModel(AccountsViewModel::class.java) }
    private val accountId: Long by lazy { arguments?.getLong("accountId") as Long  }
    private var accountAttributes: AccountAttributes? = null
    private var accountList: MutableList<BaseDetailModel> = ArrayList()
    private var currencySymbol = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_account_detail, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        accountViewModel.getAccountById(accountId).observe(this, Observer {
            accountAttributes = it[0].accountAttributes
            if (accountAttributes?.currency_symbol != null) {
                currencySymbol = accountAttributes?.currency_symbol!!
            }
            setupWidget()

        })
        recycler_view.adapter = BaseDetailRecyclerAdapter(accountList)
    }

    private fun setupWidget(){
        accountName.text = accountAttributes?.name
        accountAmount.text = currencySymbol  + " " + accountAttributes?.current_balance.toString().toBigDecimal().toPlainString()
        val data = arrayListOf(
                BaseDetailModel("Created At", accountAttributes?.created_at,
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_create).sizeDp(24)),
                BaseDetailModel("Updated At", accountAttributes?.updated_at,
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update).sizeDp(24)),
                BaseDetailModel("Active", accountAttributes?.active.toString(),
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_check).sizeDp(24)),
                BaseDetailModel("Type", accountAttributes?.type,
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_account_balance_wallet).sizeDp(24)),
                BaseDetailModel("Currency ID", accountAttributes?.currency_id.toString(),
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_attach_money).sizeDp(24)),
                BaseDetailModel("Currency Code", accountAttributes?.currency_code.toString(),
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_attach_money).sizeDp(24)),
                BaseDetailModel("Account Number", accountAttributes?.account_number,
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_account_balance).sizeDp(24)),
                BaseDetailModel("Role", accountAttributes?.role,
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_supervisor_account).sizeDp(24)),
                BaseDetailModel("Notes", accountAttributes?.notes,
                        IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_note).sizeDp(24))
                )
        accountList.addAll(data)
    }

    override fun deleteItem() {
        accountViewModel.isLoading.observe(this, Observer {
            if(it == true){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        })
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.get_confirmation)
                .setMessage(R.string.irreversible_action)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    accountViewModel.deleteAccountById(accountId).observe(this, Observer {
                        if(it == true){
                            requireFragmentManager().popBackStack()
                            toastSuccess("Account Deleted")
                        } else {
                            toastError("Account will be deleted later")
                        }
                    })
                }
                .setNegativeButton(android.R.string.no){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        /*R.id.menu_item_edit -> consume {
            val bundle = bundleOf("accountId" to accountId)
            requireFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, UpdateAccountFragment().apply { arguments = bundle })
        }*/
        R.id.menu_item_delete -> consume {
            deleteItem()
        }
        android.R.id.home -> consume {
            requireFragmentManager().popBackStack()
        }
        else -> super.onOptionsItemSelected(item)
    }
}