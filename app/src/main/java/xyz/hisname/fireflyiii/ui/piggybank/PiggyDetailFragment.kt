package xyz.hisname.fireflyiii.ui.piggybank

import android.animation.ObjectAnimator
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
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.viewmodel.retrofit.PiggyViewModel
import xyz.hisname.fireflyiii.repository.viewmodel.room.DaoPiggyViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import java.math.BigDecimal
import java.util.*

class PiggyDetailFragment: BaseDetailFragment() {

    private val currentAmount: BigDecimal by lazy { arguments?.getSerializable("currentAmount") as BigDecimal }
    private val percentage: Double by lazy { arguments?.getDouble("percentage") as Double  }
    private val leftToSave: BigDecimal by lazy { arguments?.getSerializable("leftToSave") as BigDecimal }
    private val piggyId: Long by lazy { arguments?.getLong("piggyId") as Long  }
    private val currencyCode: String by lazy { arguments?.getString("currencyCode") as String }
    private val targetAmount: BigDecimal by lazy { arguments?.getSerializable("targetAmount") as BigDecimal }
    private val name: String by lazy { arguments?.getString("name") as String }
    private val targetDate: String? by lazy { arguments?.getString("targetDate")  }
    private val minPerMonth: BigDecimal by lazy{ arguments?.getSerializable("savePerMonth") as BigDecimal }
    private val model: PiggyViewModel by lazy { getViewModel(PiggyViewModel::class.java) }
    private val dao: DaoPiggyViewModel by lazy { getViewModel(DaoPiggyViewModel::class.java) }

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
        if(!targetDate.isNullOrBlank()){
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
        model.deletePiggyBank(baseUrl,accessToken,piggyId.toString()).observe(this, Observer{
            if(it.getError() == null){
                GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT, null, {
                    dao.deletePiggyBank(piggyId)
                    withContext(Dispatchers.Main){
                        toastSuccess(resources.getString(R.string.piggy_bank_deleted), Toast.LENGTH_LONG)
                    }
                    activity?.supportFragmentManager?.popBackStack()
                })
            } else {
                ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
                val error = it.getError()
                val parentLayout: View = requireActivity().findViewById(R.id.coordinatorlayout)
                if (error!!.localizedMessage.startsWith("Unable to resolve host")) {
                    val snack = Snackbar.make(parentLayout, R.string.unable_ping_server, Snackbar.LENGTH_SHORT)
                    snack.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_red_600))
                    snack.setAction("OK") {}.show()
                } else {
                    Snackbar.make(parentLayout, R.string.generic_delete_error, Snackbar.LENGTH_LONG)
                            .setAction("Retry") {
                                deleteItem()
                            }
                            .show()
                }
            }
        })
    }


    private fun deletePiggy(){
        deletePiggyButton.setOnClickListener{
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

    private fun setupProgressBar(percentage: Int){
        val objectAnimator = ObjectAnimator.ofInt(piggyBankProgressBar, "progress",
                0, percentage)
        // 1000ms = 1s
        objectAnimator.duration = 1000
        objectAnimator.interpolator = AccelerateInterpolator()
        objectAnimator.start()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId){
        R.id.menu_item_edit -> consume {
            val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken,
                    "piggyId" to piggyId, "piggyName" to name, "targetAmount" to targetAmount)
          /*  requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, addPiggy)
                    .addToBackStack(null)
                    .commit()*/
        }
        else -> super.onOptionsItemSelected(item)
    }

}