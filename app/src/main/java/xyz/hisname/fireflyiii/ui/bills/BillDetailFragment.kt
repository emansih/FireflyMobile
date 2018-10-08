package xyz.hisname.fireflyiii.ui.bills

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_bill_detail.*
import kotlinx.android.synthetic.main.progress_overlay.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.BillsViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.room.DaoBillsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.util.extension.consume
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import java.math.BigDecimal

class BillDetailFragment: BaseDetailFragment(){

    private val model: BillsViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private val dao: DaoBillsViewModel by lazy { getViewModel(DaoBillsViewModel::class.java) }
    private val billId: Long by lazy { arguments?.getLong("billId") as Long }
    private val date: String by lazy { arguments?.getString("date") ?: "" }
    private val currencyCode: String by lazy { arguments?.getString("currencyCode") as String }
    private val billMin: BigDecimal by lazy { arguments?.getSerializable("billMin") as BigDecimal }
    private val billMax: BigDecimal by lazy { arguments?.getSerializable("billMax") as BigDecimal }
    private val billName: String by lazy { arguments?.getString("billName") as String }
    private val billFreq: String by lazy { arguments?.getString("freq") as String }
    private val billNotes: String by lazy { arguments?.getString("notes") ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_bill_detail, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWidgets()
    }

    private fun setUpWidgets(){
        deleteBill()
        billNameText.text = billName
        minAmountText.text = currencyCode + String.format(billMin.toString())
        maxAmountText.text = currencyCode + String.format(billMax.toString())
        if(date.isBlank()){
            nextDateText.text = resources.getString(R.string.no_target_date)
        } else {
            nextDateText.text = date
        }
        freqText.text = billFreq
        notesText.text = billNotes
        val deleteButton = requireActivity().findViewById<Button>(R.id.deleteBillButton)
        deleteButton.setText(R.string.delete_bill)

    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId){
        R.id.menu_item_edit -> consume {}
        else -> super.onOptionsItemSelected(item)
    }

    private fun deleteBill(){
        deleteBillButton.setOnClickListener{
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.get_confirmation)
                    .setMessage(R.string.irreversible_action)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                        ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                        deleteItem()
                    }
                    .setNegativeButton(android.R.string.no){dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

        }
    }

    override fun deleteItem() {
        model.deleteBill(baseUrl, accessToken, billId.toString()).observe(this, Observer { it ->
            if (it.getError() == null) {
                GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT, null, {
                    dao.deleteBill(billId)
                    withContext(Dispatchers.Main){
                        toastSuccess(resources.getString(R.string.bill_deleted))
                        activity?.supportFragmentManager?.popBackStack()
                    }
                })
            } else {
                val error = it.getError()
                val parentLayout: View = requireActivity().findViewById(R.id.coordinatorlayout)
                if (error!!.localizedMessage.startsWith("Unable to resolve host")) {
                    val snack = Snackbar.make(parentLayout, R.string.unable_ping_server, Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_red_600))
                    snack.setAction("OK") {}.show()
                } else {
                    Snackbar.make(parentLayout, R.string.generic_delete_error, Snackbar.LENGTH_LONG)
                            .setAction("Retry") {
                                deleteBill()
                            }
                            .show()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        AppDatabase.destroyInstance()
    }


}