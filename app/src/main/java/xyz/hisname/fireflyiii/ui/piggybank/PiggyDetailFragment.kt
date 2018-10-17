package xyz.hisname.fireflyiii.ui.piggybank

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_piggy_detail.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.PiggyBankViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.consume
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import java.math.BigDecimal
import java.util.*

class PiggyDetailFragment: BaseDetailFragment() {

    private val currentAmount: BigDecimal by lazy { arguments?.getSerializable("currentAmount") as BigDecimal }
    private val percentage: Double by lazy { arguments?.getDouble("percentage") as Double  }
    private val notes: String by lazy { arguments?.getString("notes") ?: "" }
    private val dateStarted: String by lazy { arguments?.getString("startDate") ?: "" }
    private val piggyId: Long by lazy { arguments?.getLong("piggyId") as Long  }
    private val currencyCode: String by lazy { arguments?.getString("currencyCode") as String }
    private val targetAmount: BigDecimal by lazy { arguments?.getSerializable("targetAmount") as BigDecimal }
    private val name: String by lazy { arguments?.getString("name") as String }
    private val targetDate: String by lazy { arguments?.getString("targetDate")  ?: "" }
    private val minPerMonth: BigDecimal by lazy{ arguments?.getSerializable("savePerMonth") as BigDecimal }
    private val piggyBankViewModel by lazy { getViewModel(PiggyBankViewModel::class.java)}


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_piggy_detail,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProgressBar(percentage.toInt())
        setupWidgets()
        deletePiggy()
    }

    private fun setupWidgets(){
        val deleteButton = requireActivity().findViewById<Button>(R.id.deletePiggyButton)
        deleteButton.setText(R.string.delete_piggy)
        piggyBankName.text = name
        if(percentage <= 15.toDouble()){
            piggyBankProgressBar.progressDrawable.setColorFilter(ContextCompat.getColor(requireContext(),
                    R.color.md_red_700), PorterDuff.Mode.SRC_IN)
        } else if(percentage <= 50.toDouble()){
            piggyBankProgressBar.progressDrawable.setColorFilter(ContextCompat.getColor(requireContext(),
                    R.color.md_green_500), PorterDuff.Mode.SRC_IN)
        }
        amountPercentage.text = percentage.toString() + "%"
        if(!targetDate.isBlank()){
            if(DateTimeUtil.getDaysDifference(targetDate).toInt() <= 3){
                piggyBankTargetDate.text = resources.getString(R.string.target_date, targetDate)
                piggyBankTargetDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_red_700))
            }
            if(Objects.equals(targetAmount, currentAmount)){
                piggyBankTargetDate.text = ""
            }
        } else {
            piggyBankTargetDate.text = resources.getString(R.string.no_target_date)
        }
        amount.text = resources.getString(R.string.amount, currentAmount.toString(), targetAmount.toString())
        currencyCodeTextView.text = currencyCode
        if(Objects.equals(targetAmount, currentAmount)){
            toastSuccess(resources.getString(R.string.user_did_it), Toast.LENGTH_LONG)
        }
        min_per_month.text = resources.getString(R.string.min_amount_save_per_month,
                currencyCode, minPerMonth.toString())
    }

    override fun deleteItem() {
        ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
        piggyBankViewModel.deletePiggyBank(baseUrl,accessToken, piggyId.toString()).observe(this, Observer {
            if(it.getError() == null){
                toastSuccess(resources.getString(R.string.piggy_bank_deleted), Toast.LENGTH_LONG)
                requireFragmentManager().popBackStack()
            } else {
                Snackbar.make(requireActivity().findViewById(R.id.coordinatorlayout),
                        R.string.generic_delete_error, Snackbar.LENGTH_LONG)
                        .setAction("Retry") {
                            deleteItem()
                        }
                        .show()

            }
        })
    }


    private fun deletePiggy(){
        deletePiggyButton.setOnClickListener{
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.get_confirmation)
                    .setMessage(R.string.irreversible_action)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        deleteItem()
                    }
                    .setNegativeButton(android.R.string.no){dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

        }

    }

    private fun setupProgressBar(percentage: Int){
        ObjectAnimator.ofInt(piggyBankProgressBar, "progress",
                0, percentage).apply {
            // 1000ms = 1s
            duration = 1000
            interpolator = AccelerateInterpolator()
        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId){
        R.id.menu_item_edit -> consume {
            val bundle = bundleOf("piggyId" to piggyId, "piggyName" to name, "targetAmount" to targetAmount,
                    "currentAmount" to currentAmount, "startDate" to dateStarted,"targetDate" to targetDate,
                    "notes" to notes)
            val addPiggy = Intent(requireContext(), AddPiggyActivity::class.java).apply{
                putExtras(bundle)
            }
            startActivity(addPiggy)
        }
        else -> super.onOptionsItemSelected(item)
    }

}