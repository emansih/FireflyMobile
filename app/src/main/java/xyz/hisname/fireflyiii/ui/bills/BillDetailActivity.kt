package xyz.hisname.fireflyiii.ui.bills

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_bill_detail.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.BaseDetailModel
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastSuccess

class BillDetailActivity: BaseActivity() {

    private var billList: MutableList<BaseDetailModel> = ArrayList()
    private val billViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private var billAttribute: BillAttributes? = null
    private var nameOfBill: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)
        setSupportActionBar(toolbar)
        recycler_view.layoutManager = LinearLayoutManager(this)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener { finish() }
        editBillFab.setImageDrawable(IconicsDrawable(this).icon(FontAwesome.Icon.faw_pencil_alt).sizeDp(24))
        showData()
        editBill()
    }


    private fun showData(){
        billViewModel.getBillById(intent.getLongExtra("billId", 0)).observe(this, Observer {
            billAttribute = it[0].billAttributes
            billName.text = billAttribute?.name
            nameOfBill = billAttribute?.name
            val billDataArray = arrayListOf(
                    BaseDetailModel("Updated At", billAttribute?.updated_at,
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_update).sizeDp(24)),
                    BaseDetailModel("Created At", billAttribute?.created_at,
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_create).sizeDp(24)),
                    BaseDetailModel("Currency Code", billAttribute?.currency_code,
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_local_atm).sizeDp(24)),
                    BaseDetailModel("Currency ID", billAttribute?.currency_id.toString(),
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_local_atm).sizeDp(24)),
                    BaseDetailModel("Amount Min", billAttribute?.amount_min.toString(),
                            IconicsDrawable(this@BillDetailActivity).icon(FontAwesome.Icon.faw_minus).sizeDp(24)),
                    BaseDetailModel("Amount Max", billAttribute?.amount_max.toString(),
                            IconicsDrawable(this@BillDetailActivity).icon(FontAwesome.Icon.faw_plus).sizeDp(24)),
                    BaseDetailModel("Date", billAttribute?.date,
                            ContextCompat.getDrawable(this@BillDetailActivity, R.drawable.ic_calendar_blank)),
                    BaseDetailModel("Repeat Frequency", billAttribute?.repeat_freq,
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_repeat).sizeDp(24)),
                    BaseDetailModel("Skip", billAttribute?.skip.toString(),
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_skip_next).sizeDp(24)),
                    if (billAttribute?.pay_dates.isNullOrEmpty()) {
                        BaseDetailModel("Pay Dates", "No dates found",
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    } else {
                        BaseDetailModel("Pay Dates", billAttribute?.pay_dates.toString(),
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    },
                    if (billAttribute?.paid_dates.isNullOrEmpty()) {
                        BaseDetailModel("Paid Dates", "No dates found",
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    } else {
                        BaseDetailModel("Paid Dates", billAttribute?.pay_dates.toString(),
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    },
                    BaseDetailModel("Notes", billAttribute?.notes,
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_note).sizeDp(24))
            )
            billList.addAll(billDataArray)
            recycler_view.adapter = BaseDetailRecyclerAdapter(billList)
        })
    }

    private fun editBill(){
        editBillFab.setOnClickListener {
            editBillFab.isClickable = false
            val addBill = AddBillDialog()
            addBill.arguments = bundleOf("revealX" to editBillFab.width / 2,
                    "revealY" to editBillFab.height / 2, "billId" to intent.getLongExtra("billId", 0))
            addBill.show(supportFragmentManager.beginTransaction(), "add_bill_dialog")
            editBillFab.isClickable = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.delete_menu, menu)
        return true
    }

    private fun deleteItem(){
        ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
        billViewModel.deleteBillById(intent.getLongExtra("billId", 0)).observe(this, Observer {
            if(it == true){
                finish()
                toastSuccess("Bill Deleted")
            } else {
                ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
                val parentLayout: View = findViewById(R.id.coordinatorlayout)
                Snackbar.make(parentLayout, R.string.generic_delete_error, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { _ ->
                            deleteItem()
                        }
                        .show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.menu_item_delete) {
            AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.get_confirmation))
                    .setMessage(resources.getString(R.string.delete_bill, nameOfBill))
                    .setPositiveButton("Yes") { _, _ ->
                        deleteItem()
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .show()
        }
        return true
    }

}