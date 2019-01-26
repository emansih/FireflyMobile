package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.Data
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import xyz.hisname.fireflyiii.workers.transaction.DeleteTransactionWorker

class DeleteTransactionDialog: BaseFragment() {

    private val transactionDescription by lazy { arguments?.getString("transactionDescription", "") ?: "" }
    private val transactionId by lazy { arguments?.getLong("transactionId", 0) ?: 0L }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_account_title, transactionDescription))
                .setMessage(resources.getString(R.string.delete_transaction_message, transactionDescription))
                .setIcon(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_trash)
                        .sizeDp(24)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_600)))
                .setPositiveButton(R.string.delete_permanently) { _, _ ->
                    ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
                    transactionViewModel.deleteTransaction(transactionId).observe(this, Observer {
                        ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                        if (it == true) {
                            toastSuccess(resources.getString(R.string.transaction_deleted))
                            requireFragmentManager().popBackStack()
                        } else {
                            DeleteTransactionWorker.setupWorker(Data.Builder(), transactionId)
                            toastInfo("There was an issue deleting your transaction. It will be " +
                                    "deleted in the background.",
                                    Toast.LENGTH_LONG)
                        }
                    })
                }
                .setNegativeButton("No") { _, _ ->
                    toastInfo("Transaction not deleted")
                }
                .show()
    }
}