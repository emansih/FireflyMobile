package xyz.hisname.fireflyiii.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_date_range.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DateRangeViewModel
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.DialogDarkMode
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.isBlank
import java.util.*

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
        val calendar = Calendar.getInstance()
        val startDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                startDateText = DateTimeUtil.getCalToString(calendar.timeInMillis.toString())
                startDateEditText.setText(startDateText)
            }
        }
        startDateEditText.setOnClickListener {
            DialogDarkMode().showCorrectDatePickerDialog(requireContext(), startDate, calendar)
        }
        val endDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                endDateText = DateTimeUtil.getCalToString(calendar.timeInMillis.toString())
                endDateEditText.setText(endDateText)
            }
        }
        endDateEditText.setOnClickListener {
            DialogDarkMode().showCorrectDatePickerDialog(requireContext(), endDate, calendar)
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