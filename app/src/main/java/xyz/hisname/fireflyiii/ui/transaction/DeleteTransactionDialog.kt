package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess

class DeleteTransactionDialog: BaseFragment() {

    private val transactionDescription by lazy { arguments?.getString("transactionDescription", "") ?: "" }
    private val transactionJournalId by lazy { arguments?.getLong("transactionJournalId", 0) ?: 0L }
    private val transactionInfo by lazy { arguments?.getString("transactionInfo", "") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                            parentFragmentManager.popBackStack()
                            val v2Layout = requireActivity().findViewById<LinearLayout>(R.id.fragment_transaction_rootview)
                            if(v2Layout != null){
                                v2Layout.isVisible = true
                                val mainToolbar = requireActivity().findViewById<Toolbar>(R.id.activity_toolbar)
                                mainToolbar.title = transactionInfo
                            }
                        } else {
                            toastInfo("There was an issue deleting your transaction. It will be " +
                                    "deleted in the background.",
                                    Toast.LENGTH_LONG)
                        }
                    }
                }
                .setNegativeButton("No") { _, _ ->
                    toastInfo("Transaction not deleted")
                }
                .show()
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}