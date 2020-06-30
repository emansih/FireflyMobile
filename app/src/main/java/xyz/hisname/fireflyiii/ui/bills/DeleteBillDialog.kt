package xyz.hisname.fireflyiii.ui.bills

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getViewModel

class DeleteBillDialog: DialogFragment() {
    
    private val billId by lazy { arguments?.getLong("billId") ?: 0 }
    private val billDescription by lazy { arguments?.getString("billDescription") ?: 0 }
    private val billViewModel: BillsViewModel by lazy { getViewModel(BillsViewModel::class.java) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_bill_message, billDescription))
                .setMessage(resources.getString(R.string.delete_bill_message, billDescription))
                .setIcon(IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_trash
                    sizeDp = 24
                    colorRes = R.color.md_green_600
                })
                .setPositiveButton(R.string.delete_permanently){ _,_ ->
                    billViewModel.deleteBillById(billId).observe(viewLifecycleOwner) { billDeleted ->
                        if(billDeleted){
                            toastSuccess(resources.getString(R.string.bill_deleted, billDescription))
                            dialog?.dismiss()
                            parentFragmentManager.commit {
                                remove(parentFragmentManager.findFragmentByTag("add_bill_dialog") ?: Fragment())
                            }
                        } else {
                            toastError(resources.getString(R.string.issue_deleting, "bill"),
                                    Toast.LENGTH_LONG)
                        }
                    }
                }
                .setNegativeButton("No") { _, _ ->
                    toastInfo("Bill not deleted")
                }
                .create()
    }
}