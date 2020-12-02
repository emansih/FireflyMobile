package xyz.hisname.fireflyiii.ui.bills.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.fragment_bill_details.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillPaidDates
import xyz.hisname.fireflyiii.repository.models.bills.BillPayDates
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getCompatColor
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class BillDetailsFragment: BaseDetailFragment() {

    private var selectedDate: LocalDate? = null
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val billDetailsViewModel by lazy { getImprovedViewModel(BillDetailsViewModel::class.java) }
    private val billId: Long by lazy { arguments?.getLong("billId") ?: 0  }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_bill_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPaidDates(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
    }

    private fun getPaidDates(startDate: String, endDate: String){
        billDetailsViewModel.getPayList(billId, startDate, endDate).observe(viewLifecycleOwner){ billPayList ->
            setPaidCalendar(billPayList)
        }
    }

    private fun setPaidCalendar(billPaidList: List<BillPayDates>){
        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay
            val onDayText = view.dayText
            init {
                view.setOnClickListener {
                    selectedDate = day.date
                    paidDatesCalendarView.notifyCalendarChanged()
                }
            }
        }

        paidDatesCalendarView.dayBinder = object: DayBinder<DayViewContainer>{
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.onDayText
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    if (billPaidList.isNotEmpty()){
                        billPaidList.forEach { data ->
                            if(data.payDates == day.date){
                                textView.setBackgroundColor(getCompatColor(R.color.md_red_400))
                            } else {
                                textView.setTextColor(setDayNightTheme())
                            }
                        }
                    } else {
                        textView.setTextColor(setDayNightTheme())
                    }
                } else {
                    if(globalViewModel.isDark){
                        textView.setTextColor(getCompatColor(R.color.md_black_1000))
                    } else {
                        textView.setTextColor(getCompatColor(R.color.md_white_1000))
                    }
                }
            }

            override fun create(view: View): DayViewContainer = DayViewContainer(view)
        }
        paidDatesCalendarView.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            paidDatesHeaderText.text = title
        }
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(40)
        val endMonth = currentMonth.plusYears(30)
        paidDatesCalendarView.setup(startMonth, endMonth,
                WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        paidDatesCalendarView.scrollToMonth(currentMonth)
        paidDatesCalendarView.updateMonthConfiguration()
    }

    private fun setDayNightTheme(): Int{
        return if(globalViewModel.isDark){
            getCompatColor(R.color.md_white_1000)
        } else {
            getCompatColor(R.color.md_black_1000)
        }
    }

    override fun deleteItem() {
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

}