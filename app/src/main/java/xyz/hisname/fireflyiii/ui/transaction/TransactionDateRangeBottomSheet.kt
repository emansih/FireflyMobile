package xyz.hisname.fireflyiii.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_date_range.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DateRangeViewModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.isBlank

class TransactionDateRangeBottomSheet: BottomSheetDialogFragment() {

    private lateinit var startDateText: String
    private lateinit var endDateText: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_date_range, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDate()
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
                val dateRange = getViewModel(DateRangeViewModel::class.java)
                dateRange.startDate(startDateText)
                dateRange.endDate(endDateText)
                dismiss()
            }
        }
    }

}