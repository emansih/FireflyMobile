package xyz.hisname.fireflyiii.ui.bills

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess

class DeleteBillDialog: DialogFragment() {
    
    private val billId by lazy { arguments?.getLong("billId") ?: 0 }
    private val billDescription by lazy { arguments?.getString("billDescription") ?: 0 }
    private val billViewModel: BillsViewModel by lazy { getViewModel(BillsViewModel::class.java) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.get_confirmation)
                .setMessage(resources.getString(R.string.delete_bill, billDescription))
                .setIcon(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_trash)
                        .sizeDp(24)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_600)))
                .setPositiveButton("Yes"){ _,_ ->
                    billViewModel.deleteBillById(billId).observe(this, Observer {
                        if(it == true){
                            toastSuccess(resources.getString(R.string.bill_deleted))
                            dialog?.dismiss()
                            requireFragmentManager().commit {
                                remove(requireFragmentManager().findFragmentByTag("add_bill_dialog") ?: Fragment())
                            }
                        } else {
                            toastError(resources.getString(R.string.issue_deleting, "bill"),
                                    Toast.LENGTH_LONG)
                        }
                    })
                }
                .setNegativeButton("No") { _, _ ->
                    toastInfo("Bill not deleted")
                }
                .create()
    }
}