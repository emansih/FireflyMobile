package xyz.hisname.fireflyiii.ui.bills

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_bill_detail.*
import kotlinx.android.synthetic.main.progress_overlay.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.BaseDetailModel
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.repository.bills.BillsViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseActivity
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess

class BillDetailActivity: BaseActivity() {

    private var billList: MutableList<BaseDetailModel> = ArrayList()
    private val billViewModel by lazy { getViewModel(BillsViewModel::class.java) }
    private var billAttribute: BillAttributes? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)
        setSupportActionBar(toolbar)
        runLayoutAnimation(recycler_view)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.setNavigationOnClickListener { finish() }
        showData()
        editBill()
    }


    private fun showData(){
        billViewModel.getBillById(intent.getLongExtra("billId", 0)).observe(this, Observer {
            billAttribute = it[0].billAttributes
            billName.text = billAttribute?.name
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
                    BaseDetailModel("Automatch", billAttribute?.automatch.toString(),
                            IconicsDrawable(this@BillDetailActivity).icon(FontAwesome.Icon.faw_magic).sizeDp(24)),
                    if (billAttribute?.pay_dates!!.isEmpty()) {
                        BaseDetailModel("Pay Dates", "No dates found",
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    } else {
                        BaseDetailModel("Pay Dates", billAttribute?.pay_dates.toString(),
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    },
                    if (billAttribute?.paid_dates!!.isEmpty()) {
                        BaseDetailModel("Paid Dates", "No dates found",
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    } else {
                        BaseDetailModel("Paid Dates", billAttribute?.pay_dates.toString(),
                                IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_credit_card).sizeDp(24))
                    },
                    BaseDetailModel("Notes", billAttribute?.markdown,
                            IconicsDrawable(this@BillDetailActivity).icon(GoogleMaterial.Icon.gmd_note).sizeDp(24))
            )
            billList.addAll(billDataArray)
        })
        recycler_view.adapter = BaseDetailRecyclerAdapter(billList)
    }

    private fun editBill(){
        editBillFab.setOnClickListener {
            val data = Gson()
            val billDetail = Intent(this, AddBillActivity::class.java).apply {
                putExtras(bundleOf("billId" to intent.getLongExtra("billId", 0),
                        "status" to "UPDATE", "billData" to data.toJson(billAttribute)))
            }
            startActivity(billDetail)
        }

    }

    private fun runLayoutAnimation(recyclerView: RecyclerView){
        val controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BillDetailActivity)
            layoutAnimation = controller
            adapter?.notifyDataSetChanged()
            scheduleLayoutAnimation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
                    .setTitle("Are you sure?")
                    .setMessage(R.string.irreversible_action)
                    .setPositiveButton("Yes") { _, _ ->
                        deleteItem()
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .show()
        }
        return true
    }

}