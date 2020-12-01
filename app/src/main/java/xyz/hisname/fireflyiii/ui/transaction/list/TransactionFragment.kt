package xyz.hisname.fireflyiii.ui.transaction.list

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.android.synthetic.main.fragment_transaction.slider
import kotlinx.android.synthetic.main.recent_transaction_list.view.*
import me.toptas.fancyshowcase.FancyShowCaseQueue
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionAdapter
import xyz.hisname.fireflyiii.ui.transaction.TransactionMonthRecyclerView
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionFragment
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class TransactionFragment: BaseFragment(){

    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val selectedDates = mutableListOf<LocalDate>()
    private val dateRange by lazy { mutableSetOf<LocalDate>() }
    private var startDate = LocalDate.now()
    private var endDate = LocalDate.now()
    private lateinit var result: ActionBarDrawerToggle
    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private val noTransactionText by bindView<TextView>(R.id.listText)
    private val noTransactionImage by bindView<ImageView>(R.id.listImage)
    private lateinit var transactionVm: TransactionFragmentViewModel
    private val transactionAdapter by lazy { TransactionAdapter{ data -> itemClicked(data) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setupFab()
        transactionVm = getImprovedViewModel(TransactionFragmentViewModel::class.java)
        result = ActionBarDrawerToggle(requireActivity(),
                fragment_transaction_root, invisibleToolbar,
                com.mikepenz.materialdrawer.R.string.material_drawer_open,
                com.mikepenz.materialdrawer.R.string.material_drawer_close)
        fragment_transaction_root.addDrawerListener(result)
        displayResult()
        setWidgets()
        setCalendar()
        setTransactionCard()
        jumpToDate()
        dragAndDrop()
    }


    private fun setWidgets(){
        loadTransaction()
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
        swipeContainer.setOnRefreshListener {
            transactionAdapter.refresh()
        }
    }

    private fun loadTransaction(){
        transactionVm.getTransactionList(startDate.toString(), endDate.toString(),
                transactionType).observe(viewLifecycleOwner){ pagingData ->
            transactionAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun itemClicked(data: Transactions){
        fragment_transaction_rootview.isVisible = false
        parentFragmentManager.commit {
            add(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
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
                setShowCase(textView)
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
                            textView.setTextColor(setDayNightTheme())
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        dateRange.contains(day.date) -> {
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        selectedDate == day.date -> {
                            if(selectedDate == today){
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
                    if(globalViewModel.isDark){
                        textView.setTextColor(getCompatColor(R.color.md_black_1000))
                    } else {
                        textView.setTextColor(getCompatColor(R.color.md_white_1000))
                    }
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
        transaction_calendar.setupAsync(startMonth, endMonth, DayOfWeek.SUNDAY){
            transaction_calendar.scrollToMonth(currentMonth)
            transaction_calendar.updateMonthConfiguration()
        }
    }

    private fun dragAndDrop(){
        recycler_view.enableDragDrop(extendedFab){ viewHolder, isCurrentlyActive ->
            if(viewHolder.itemView.list_item.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive) {
                    swipeContainer.isRefreshing = true
                    val transactionJournalId = viewHolder.itemView.transactionJournalId.text.toString()
                    val transactionName = viewHolder.itemView.transactionNameText.text
                    transactionVm.deleteTransaction(transactionJournalId).observe(viewLifecycleOwner){ isDeleted ->
                        transactionAdapter.refresh()
                        swipeContainer.isRefreshing = false
                        if(isDeleted){
                            toastSuccess(resources.getString(R.string.transaction_deleted))
                        } else {
                            toastOffline(resources.getString(R.string.data_will_be_deleted_later,
                                    transactionName), Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
    }

    private fun setDayNightTheme(): Int{
        return if(globalViewModel.isDark){
            getCompatColor(R.color.md_white_1000)
        } else {
            getCompatColor(R.color.md_black_1000)
        }
    }

    private fun setTransactionCard(){
        transactionCardLoader.show()
        result.drawerArrowDrawable.color = setDayNightTheme()
        slider.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        transactionVm.getTransactionAmount(transactionType).observe(viewLifecycleOwner){ transactionArray ->
            transactionCardLoader.hide()
            slider.recyclerView.adapter = TransactionMonthRecyclerView(transactionArray){ data: Int -> }
        }
        fragment_transaction_root.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                extendedFab.hide()
            }

            override fun onDrawerClosed(drawerView: View) {
                extendedFab.show()
            }

            override fun onDrawerStateChanged(newState: Int) {
                if (newState == ViewDragHelper.STATE_DRAGGING){
                    extendedFab.hide()
                }
                if(newState == ViewDragHelper.STATE_IDLE && !fragment_transaction_root.isDrawerOpen(slider)){
                    extendedFab.show()
                }
            }

        })
    }

    private fun setRecyclerView(){
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recycler_view.adapter = transactionAdapter
    }

    private fun jumpToDate(){
        headerText.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder
                    .datePicker()
                    .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                selectedDates.add(DateTimeUtil.convertLongToLocalDate(time))
                transaction_calendar.smoothScrollToDate(DateTimeUtil.convertLongToLocalDate(time))
            }
        }
    }

    private fun setupFab(){
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

    private fun setShowCase(textView: TextView){
        FancyShowCaseQueue()
                .add(showCase(R.string.transaction_calendar_date_help_text, "longClickDate", textView))
                .add(showCase(R.string.transaction_calendar_header_help_text, "jumpDates", headerText, true))
                .show()
    }

    private fun displayResult(){
        transactionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
            if(loadStates.refresh !is LoadState.Loading){
                if(transactionAdapter.itemCount < 1){
                    noTransactionText.isVisible = true
                    noTransactionText.text = resources.getString(R.string.no_transaction_found, transactionType)
                    noTransactionImage.isVisible = true
                    noTransactionImage.setImageDrawable(IconicsDrawable(requireContext()).apply {
                        icon = FontAwesome.Icon.faw_exchange_alt
                        sizeDp = 24
                    })
                } else {
                    recycler_view.isVisible = true
                    noTransactionText.isGone = true
                    noTransactionImage.isGone = true
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        result.onConfigurationChanged(newConfig)
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        val toolBarTitle = when {
            transactionType.contains("Withdrawal") -> resources.getString(R.string.withdrawal)
            transactionType.contains("Deposit") -> resources.getString(R.string.deposit)
            transactionType.contains("Transfer") -> resources.getString(R.string.transfer)
            else -> ""
        }
        activity?.activity_toolbar?.title = toolBarTitle
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        val toolBarTitle = when {
            transactionType.contains("Withdrawal") -> resources.getString(R.string.withdrawal)
            transactionType.contains("Deposit") -> resources.getString(R.string.deposit)
            transactionType.contains("Transfer") -> resources.getString(R.string.transfer)
            else -> ""
        }
        activity?.activity_toolbar?.title = toolBarTitle
    }
}