package xyz.hisname.fireflyiii.ui.bills.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.bills_list_item.view.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.fragment_bill_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.bills.BillData
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.bills.AddBillFragment
import xyz.hisname.fireflyiii.ui.bills.BillsRecyclerAdapter
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class ListBillFragment: BaseFragment() {

    private val billAdapter by lazy { BillsRecyclerAdapter { data: BillData -> itemClicked(data)}  }
    private val billViewModel by lazy { getImprovedViewModel(ListBillViewModel::class.java) }
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_bill_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCalendar()
        setRecyclerView()
        loadBill(today.toString())
        pullToRefresh()
        initFab()
        jumpToDate()
    }

    private fun setRecyclerView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = billAdapter
        recycler_view.enableDragDrop(extendedFab) { viewHolder, isCurrentlyActive ->
            if (viewHolder.itemView.billCard.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive){
                    val billName = viewHolder.itemView.billId.text.toString()
                    billViewModel.deleteBillById(billName).observe(viewLifecycleOwner){ isDeleted ->
                        billAdapter.refresh()
                        if(isDeleted){
                            toastSuccess(resources.getString(R.string.bill_deleted, billName))
                        } else {
                            toastOffline(resources.getString(R.string.data_will_be_deleted_later, billName), Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    }

    private fun setCalendar(){
        class DayViewContainer(view: View): ViewContainer(view) {
            val onDayText = view.dayText
            lateinit var day: CalendarDay
            init {
                selectedDate = today
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectedDate = day.date
                        bill_calendar.notifyCalendarChanged()
                        loadBill(selectedDate.toString())
                    }
                }
            }
        }
        bill_calendar.dayBinder = object: DayBinder<DayViewContainer> {
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.onDayText
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        selectedDate == day.date -> {
                            if (selectedDate == today) {
                                textView.setTextColor(getCompatColor(R.color.md_red_800))
                            } else {
                                textView.setTextColor(setDayNightTheme())
                            }
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        else -> {
                            textView.setTextColor(setDayNightTheme())
                            textView.background = null
                        }
                    }
                } else {
                    if (globalViewModel.isDark) {
                        textView.setTextColor(getCompatColor(R.color.md_black_1000))
                    } else {
                        textView.setTextColor(getCompatColor(R.color.md_white_1000))
                    }
                }
            }

            override fun create(view: View) = DayViewContainer(view)
        }
        bill_calendar.monthScrollListener = { calendarMonth ->
            headerText.text = monthTitleFormatter.format(calendarMonth.yearMonth) + " " + calendarMonth.yearMonth.year
        }
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(80)
        val endMonth = currentMonth.plusYears(20)
        bill_calendar.setupAsync(startMonth, endMonth, DayOfWeek.SUNDAY){
            bill_calendar.scrollToMonth(currentMonth)
            bill_calendar.updateMonthConfiguration()
        }
    }

    private fun loadBill(startDate: String){
        billViewModel.getBillList(startDate).observe(viewLifecycleOwner){
            billAdapter.submitData(lifecycle, it)
        }
    }

    private fun setDayNightTheme(): Int{
        return if(globalViewModel.isDark){
            getCompatColor(R.color.md_white_1000)
        } else {
            getCompatColor(R.color.md_black_1000)
        }
    }

    private fun itemClicked(billData: BillData){
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddBillFragment().apply {
                arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2, "billId" to billData.billId)
            })
            addToBackStack(null)
        }
    }

    private fun jumpToDate(){
        headerText.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder
                    .datePicker()
                    .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                bill_calendar.smoothScrollToDate(DateTimeUtil.convertLongToLocalDate(time))
            }
        }
    }

    private fun initFab(){
        extendedFab.display {
            extendedFab.isClickable = false
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, AddBillFragment().apply {
                    arguments = bundleOf("revealX" to extendedFab.width / 2, "revealY" to extendedFab.height / 2)
                })
                addToBackStack(null)
            }
            extendedFab.isClickable = true
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            billAdapter.refresh()
        }
        billAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading) {
                if(billAdapter.itemCount < 1){
                    listText.text = resources.getString(R.string.no_bills)
                    listImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                        icon = GoogleMaterial.Icon.gmd_insert_emoticon
                        sizeDp = 24
                    })
                    listText.isVisible = true
                    listImage.isVisible = true
                } else {
                    listText.isVisible = false
                    listImage.isVisible = false
                    recycler_view.isVisible = true
                }
            }
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.bill)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.bill)
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}