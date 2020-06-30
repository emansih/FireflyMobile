package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_date_range.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DateRangeViewModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.isBlank

class TransactionDateRangeBottomSheet: BottomSheetDialogFragment() {

    private var startDateText: String? = null
    private var endDateText: String? = null
    private val dateRange by lazy { getViewModel(DateRangeViewModel::class.java) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_date_range, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextField()
        setDate()
    }

    private fun setTextField(){
        dateRange.startDate.observe(viewLifecycleOwner){
            startDateEditText.setText(it)
        }
        dateRange.endDate.observe(viewLifecycleOwner){
            endDateEditText.setText(it)
        }
    }

    private fun setDate(){
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val picker = builder.build()
        startDateEditText.setOnClickListener {
            picker.show(parentFragmentManager, "datepicker")
        }
        endDateEditText.setOnClickListener {
            picker.show(parentFragmentManager, "datepicker")
        }
        picker.addOnPositiveButtonClickListener {
            startDateText = DateTimeUtil.getCalToString(picker.selection?.first.toString())
            endDateText = DateTimeUtil.getCalToString(picker.selection?.second.toString())
            startDateEditText.setText(startDateText)
            endDateEditText.setText(endDateText)
        }
        applyButton.setOnClickListener{
            if(startDateEditText.isBlank() or endDateEditText.isBlank()){
                onDestroy()
            } else {
                dateRange.startDate(startDateText)
                dateRange.endDate(endDateText)
                dismiss()
            }
        }
    }

}