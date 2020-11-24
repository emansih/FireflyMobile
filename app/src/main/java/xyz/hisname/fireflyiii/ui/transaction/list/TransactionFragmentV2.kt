package xyz.hisname.fireflyiii.ui.transaction.list

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.coroutines.flow.collectLatest
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class TransactionFragmentV2: BaseTransactionFragment(){

    private lateinit var layoutManager: LinearLayoutManager
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val selectedDates = mutableListOf<LocalDate>()
    private val dateRange by lazy { mutableSetOf<LocalDate>() }
    private var startDate = LocalDate.now()
    private var endDate = LocalDate.now()
    private val transactionAdapter by lazy { TransactionAdapter{ data -> itemClicked(data) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidgets()
        setCalendar()
    }


    private fun setWidgets(){
        layoutManager = LinearLayoutManager(requireContext())
        recycler_view.layoutManager = layoutManager
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = transactionAdapter
        loadTransaction()
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && extendedFab.isShown) {
                    extendedFab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    extendedFab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        swipeContainer.setOnRefreshListener {
            loadTransaction()
        }
    }

    private fun loadTransaction(){
        transactionVm.getTransactionList(startDate.toString(), endDate.toString(),
                transactionType).observe(viewLifecycleOwner){ pagingData ->
            transactionAdapter.submitData(lifecycle, pagingData)
        }
        transactionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading){
                if(transactionAdapter.itemCount < 1){
                    displayResults(false)
                } else {
                    displayResults(true)
                }
            }
        }
    }

    override fun itemClicked(data: Transactions){
        fragment_transaction_rootview.isVisible = false
        parentFragmentManager.commit {
            add(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    override fun setupFab(){
        extendedFab.display{
            val addTransaction = AddTransactionFragment()
            addTransaction.arguments = bundleOf("transactionType" to transactionType,
                    "SHOULD_HIDE" to true)
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, addTransaction)
                addToBackStack(null)
            }
            extendedFab.isVisible = false
            fragmentContainer.isVisible = false
        }
    }

    private fun setCalendar(){
        class DayViewContainer(view: View): ViewContainer(view) {
            val onDayText = view.dayText
            lateinit var day: CalendarDay

            init {
                selectedDate = today
                view.setOnClickListener {
                    selectedDates.clear()
                    dateRange.clear()
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectedDate = day.date
                        selectedDates.clear()
                        transaction_calendar.notifyCalendarChanged()
                        startDate = selectedDate
                        endDate = selectedDate
                        loadTransaction()
                    }
                }

                view.setOnLongClickListener {
                    selectedDate = null
                    val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (selectedDates.contains(day.date)) {
                        selectedDates.remove(day.date)
                    } else {
                        if (selectedDates.size >= 2){
                            selectedDates.clear()
                        }
                        if(selectedDates.isEmpty()){
                            selectedDates.add(day.date)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                //deprecated in API 26
                                vibrator.vibrate(100)
                            }
                        } else if(selectedDates.isNotEmpty() && selectedDates[0].isAfter(day.date)){
                            toastInfo("Select earlier date first!", Toast.LENGTH_SHORT)
                        } else {
                            selectedDates.add(day.date)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                //deprecated in API 26
                                vibrator.vibrate(100)
                            }
                        }
                    }
                    transaction_calendar.notifyCalendarChanged()
                    true
                }
            }
        }
        transaction_calendar.dayBinder = object: DayBinder<DayViewContainer>{
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.onDayText
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        selectedDates.contains(day.date) -> {
                            if (selectedDates.size == 2){
                                for (x in 0 until DateTimeUtil.getDaysDifference(selectedDates[0], selectedDates[1])){
                                    dateRange.add(selectedDates[1].minusDays(x))
                                }
                                startDate = selectedDates[0]
                                endDate = selectedDates[1]
                                loadTransaction()
                            } else {
                                dateRange.clear()
                            }
                            textView.setTextColor(getCompatColor(R.color.md_white_1000))
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        dateRange.contains(day.date) -> {
                            textView.setTextColor(getCompatColor(R.color.md_white_1000))
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        selectedDate == day.date -> {
                            if(selectedDate == today){
                                textView.setTextColor(getCompatColor(R.color.md_red_600))
                            } else {
                                textView.setTextColor(getCompatColor(R.color.md_white_1000))
                            }
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        else -> {
                            textView.setTextColor(getCompatColor(R.color.md_white_1000))
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColor(getCompatColor(R.color.md_black_1000))
                }
            }

            override fun create(view: View) = DayViewContainer(view)

        }

        transaction_calendar.monthScrollListener = { calendarMonth ->
            headerText.text = monthTitleFormatter.format(calendarMonth.yearMonth) + " " + calendarMonth.yearMonth.year
        }
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(80)
        val endMonth = currentMonth.plusYears(20)
        transaction_calendar.setup(startMonth, endMonth, WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        transaction_calendar.scrollToMonth(currentMonth)
        transaction_calendar.updateMonthConfiguration()
    }

}