package xyz.hisname.fireflyiii.ui.piggybank

import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_piggy_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyAttributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.util.extension.*
import java.math.BigDecimal
import kotlin.collections.ArrayList

class PiggyDetailFragment: BaseDetailFragment() {

    private val piggyId: Long by lazy { arguments?.getLong("piggyId") as Long  }
    private var piggyAttribute: PiggyAttributes? = null
    private var currentAmount: BigDecimal? = 0.toBigDecimal()
    private var percentage: Int = 0
    private var currencyCode: String = ""
    private var piggyList: MutableList<DetailModel> = ArrayList()
    private var piggyName: String? = ""
    private var accountId: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_piggy_detail,container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        piggyViewModel.getPiggyById(piggyId).observe(viewLifecycleOwner) {
            piggyAttribute = it[0].piggyAttributes
            piggyName = piggyAttribute?.name
            currentAmount = piggyAttribute?.current_amount
            percentage = piggyAttribute?.percentage ?: 0
            currencyCode = piggyAttribute?.currency_code ?: ""
            accountId = piggyAttribute?.account_id ?: 0L
            setupWidgets()
            setupProgressBar()
        }
        // TODO: Remove this dirty hack. globalViewModel and scope should be private!
        globalViewModel.backPress.observe(viewLifecycleOwner){ backPressValue ->
            if(backPressValue == true) {
                scope.launch(Dispatchers.Main) {
                    handleBack()
                }.invokeOnCompletion {
                    globalViewModel.backPress.value = false
                }
            }
        }
    }


    private fun setupWidgets(){
        val piggy = mutableListOf(
                DetailModel(resources.getString(R.string.account), piggyAttribute?.account_name),
                DetailModel(resources.getString(R.string.target_amount), piggyAttribute?.target_amount.toString()),
                DetailModel(resources.getString(R.string.saved_so_far), piggyAttribute?.save_per_month.toString()),
                DetailModel(resources.getString(R.string.left_to_save), piggyAttribute?.left_to_save.toString()),
                DetailModel(resources.getString(R.string.start_date), piggyAttribute?.start_date),
                if(piggyAttribute?.target_date == null){
                    DetailModel(resources.getString(R.string.target_date), "")
                } else {
                    DetailModel(resources.getString(R.string.target_date), piggyAttribute?.target_date)
                }
        )
        runLayoutAnimation(piggy_bank_details_recyclerview)
        piggy_bank_details_recyclerview.adapter = BaseDetailRecyclerAdapter(piggy){ position: Int -> setClickListener(position)}
        piggyList.addAll(piggy)
        if(percentage <= 15){
            piggyBankProgressBar.progressDrawable.setColorFilter(getCompatColor(
                    R.color.md_red_700), PorterDuff.Mode.SRC_IN)
        } else if(percentage <= 50){
            piggyBankProgressBar.progressDrawable.setColorFilter(getCompatColor(
                    R.color.md_green_500), PorterDuff.Mode.SRC_IN)
        }
        amount.text = currentAmount.toString()
        amountPercentage.text = percentage.toString() + "%"
        currencyCodeTextView.text = currencyCode
        piggyBankName.text = piggyName
    }

    private fun setClickListener(position: Int){
        when(position){
            0 -> {
                parentFragmentManager.commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to accountId)
                    })
                    addToBackStack(null)
                }
            }
        }
    }

    override fun deleteItem() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_piggy_bank_title, piggyName))
                .setMessage(resources.getString(R.string.delete_piggy_bank_message, piggyName))
                .setPositiveButton(R.string.delete_permanently) { dialog, _ ->
                    ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
                    piggyViewModel.deletePiggyById(piggyId).observe(viewLifecycleOwner) {
                        ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                        if(it){
                            parentFragmentManager.popBackStack()
                            toastSuccess(resources.getString(R.string.piggy_bank_deleted, piggyName))
                        } else {
                            val parentLayout: View = requireActivity().findViewById(R.id.coordinatorlayout)
                            Snackbar.make(parentLayout, R.string.generic_delete_error, Snackbar.LENGTH_LONG)
                                    .setAction("Retry") { _ ->
                                        deleteItem()
                                    }
                                    .show()
                        }
                    }
                }
                .setNegativeButton(android.R.string.no){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }


    private fun setupProgressBar(){
        ObjectAnimator.ofInt(piggyBankProgressBar, "progress", 0, percentage).apply {
            // 1000ms = 1s
            duration = 1000
            interpolator = AccelerateInterpolator()
        }.start()
    }

    override fun editItem() {
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddPiggyFragment().apply {
                arguments = bundleOf("piggyId" to piggyId)
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        piggyList.clear()
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}