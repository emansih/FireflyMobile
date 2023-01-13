/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import androidx.appcompat.widget.Toolbar
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
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.sizeDp
import me.toptas.fancyshowcase.FancyShowCaseQueue
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.BaseSwipeLayoutBinding
import xyz.hisname.fireflyiii.databinding.CalendarDayBinding
import xyz.hisname.fireflyiii.databinding.FragmentTransactionBinding
import xyz.hisname.fireflyiii.databinding.RecentTransactionListBinding
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionMonthRecyclerView
import xyz.hisname.fireflyiii.ui.transaction.TransactionMonthSummaryFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionSeparatorAdapter
import xyz.hisname.fireflyiii.ui.transaction.addtransaction.AddTransactionPager
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
    private val transactionAdapter by lazy { TransactionSeparatorAdapter{ data -> itemClicked(data) } }
    private var fragmentTransactionBinding: FragmentTransactionBinding? = null
    private val binding get() = fragmentTransactionBinding!!
    private var baseSwipeLayoutBinding: BaseSwipeLayoutBinding? = null
    private val baseBinding get() = baseSwipeLayoutBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentTransactionBinding = FragmentTransactionBinding.inflate(inflater, container, false)
        baseSwipeLayoutBinding = binding.baseLayout.baseSwipeLayout
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setupFab()
        transactionVm = getImprovedViewModel(TransactionFragmentViewModel::class.java)
        result = ActionBarDrawerToggle(requireActivity(),
                binding.fragmentTransactionRoot, binding.invisibleToolbar,
                com.mikepenz.materialdrawer.R.string.material_drawer_open,
                com.mikepenz.materialdrawer.R.string.material_drawer_close)
        binding.fragmentTransactionRoot.addDrawerListener(result)
        displayResult()
        setWidgets()
        setCalendar()
        setTransactionCard()
        jumpToDate()
        dragAndDrop()
    }


    private fun setWidgets(){
        loadTransaction()
        baseBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        baseBinding.swipeContainer.setOnRefreshListener {
            transactionAdapter.refresh()
        }
        binding.buttonSummaryPanel.setOnClickListener {
            extendedFab.hide()
            binding.fragmentTransactionRoot.openDrawer(binding.slider)
        }
    }

    private fun loadTransaction(){
        transactionVm.getTransactionList(startDate.toString(), endDate.toString(),
                transactionType).observe(viewLifecycleOwner){ pagingData ->
            transactionAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun itemClicked(data: Transactions){
        parentFragmentManager.commit {
            replace(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    private fun setCalendar(){
        class DayViewContainer(view: View): ViewContainer(view) {
            val calendarDayBinding = CalendarDayBinding.bind(view)
            val onDayText = calendarDayBinding.dayText
            lateinit var day: CalendarDay
            init {
                selectedDate = today
                view.setOnClickListener {
                    selectedDates.clear()
                    dateRange.clear()
                    if (day.position == DayPosition.MonthDate) {
                        selectedDate = day.date
                        selectedDates.clear()
                        binding.transactionCalendar.notifyCalendarChanged()
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
                    if(selectedDates.size == 2){
                        for (x in 0 until DateTimeUtil.getDaysDifference(selectedDates[0], selectedDates[1])){
                            dateRange.add(selectedDates[1].minusDays(x))
                        }
                        startDate = selectedDates[0]
                        endDate = selectedDates[1]
                        loadTransaction()
                    }
                    binding.transactionCalendar.notifyCalendarChanged()
                    baseBinding.recyclerView.scrollToPosition(1)
                    true
                }
            }
        }
        binding.transactionCalendar.dayBinder = object: MonthDayBinder<DayViewContainer>{
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.onDayText
                textView.text = data.date.dayOfMonth.toString()
                setShowCase(textView)
                if (data.position == DayPosition.MonthDate) {
                    when {
                        selectedDates.contains(data.date) -> {
                            if (selectedDates.size != 2){
                                dateRange.clear()
                            }
                            textView.setTextColor(setDayNightTheme())
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        dateRange.contains(data.date) -> {
                            textView.setBackgroundResource(R.drawable.today_bg)
                        }
                        selectedDate == data.date -> {
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

        binding.transactionCalendar.monthScrollListener = { calendarMonth ->
            binding.headerText.text = monthTitleFormatter.format(calendarMonth.yearMonth) + " " + calendarMonth.yearMonth.year
        }
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(80)
        val endMonth = currentMonth.plusYears(20)
        binding.transactionCalendar.setup(startMonth, endMonth, DayOfWeek.SUNDAY)
        binding.transactionCalendar.scrollToMonth(currentMonth)
        binding.transactionCalendar.updateMonthData()
    }

    private fun dragAndDrop(){
        baseBinding.recyclerView.enableDragDrop(extendedFab){ viewHolder, isCurrentlyActive ->
            val transactionListBinding = RecentTransactionListBinding.bind(viewHolder.itemView)
            if(transactionListBinding.listItem.isOverlapping(extendedFab)){
                extendedFab.dropToRemove()
                if(!isCurrentlyActive) {
                    baseBinding.swipeContainer.isRefreshing = true
                    val transactionJournalId = transactionListBinding.transactionJournalId.text.toString()
                    val transactionName = transactionListBinding.transactionNameText.text
                    transactionVm.deleteTransaction(transactionJournalId).observe(viewLifecycleOwner){ isDeleted ->
                        transactionAdapter.refresh()
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
        binding.transactionCardLoader.show()
        result.drawerArrowDrawable.color = setDayNightTheme()
        binding.slider.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        transactionVm.getTransactionAmount(transactionType).observe(viewLifecycleOwner){ transactionArray ->
            binding.transactionCardLoader.hide()
            binding.slider.recyclerView.adapter = TransactionMonthRecyclerView(transactionArray){ data: Int ->
                binding.fragmentTransactionRoot.closeDrawer(binding.slider)
                parentFragmentManager.commit {
                    replace(R.id.fragment_container, TransactionMonthSummaryFragment().apply {
                        arguments = bundleOf("monthYear" to data, "transactionType" to transactionType)
                    })
                    addToBackStack(null)
                }
            }
        }
        binding.fragmentTransactionRoot.addDrawerListener(object : DrawerLayout.DrawerListener {
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
                if(newState == ViewDragHelper.STATE_IDLE && !binding.fragmentTransactionRoot.isDrawerOpen(binding.slider)){
                    extendedFab.show()
                }
            }

        })
    }

    private fun setRecyclerView(){
        baseBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        baseBinding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        baseBinding.recyclerView.adapter = transactionAdapter
    }

    private fun jumpToDate(){
        binding.headerText.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder
                    .datePicker()
                    .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                selectedDates.clear()
                selectedDates.add(DateTimeUtil.convertLongToLocalDate(time))
                binding.transactionCalendar.smoothScrollToDate(DateTimeUtil.convertLongToLocalDate(time))
            }
        }
    }

    private fun setupFab(){
        extendedFab.display{
            val addTransaction = AddTransactionPager()
            addTransaction.arguments = bundleOf("transactionType" to transactionType, "SHOULD_HIDE" to true)
            parentFragmentManager.commit {
                add(R.id.bigger_fragment_container, addTransaction)
                addToBackStack(null)
            }
        }
    }

    private fun setShowCase(textView: TextView){
        FancyShowCaseQueue()
                .add(showCase(R.string.transaction_calendar_date_help_text, "longClickDate", textView))
                .add(showCase(R.string.transaction_calendar_header_help_text, "jumpDates", binding.headerText, true))
                .show()
    }

    private fun displayResult(){
        transactionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            baseBinding.swipeContainer.isRefreshing = loadStates.refresh is LoadState.Loading
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
                    baseBinding.recyclerView.isVisible = true
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

    override fun onResume() {
        super.onResume()
        val toolBarTitle = when {
            transactionType.contains("Withdrawal") -> resources.getString(R.string.withdrawal)
            transactionType.contains("Deposit") -> resources.getString(R.string.deposit)
            transactionType.contains("Transfer") -> resources.getString(R.string.transfer)
            else -> ""
        }
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = toolBarTitle
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        val toolBarTitle = when {
            transactionType.contains("Withdrawal") -> resources.getString(R.string.withdrawal)
            transactionType.contains("Deposit") -> resources.getString(R.string.deposit)
            transactionType.contains("Transfer") -> resources.getString(R.string.transfer)
            else -> ""
        }
        requireActivity().findViewById<Toolbar>(R.id.activity_toolbar).title = toolBarTitle
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentTransactionBinding = null
        baseSwipeLayoutBinding = null
    }
}