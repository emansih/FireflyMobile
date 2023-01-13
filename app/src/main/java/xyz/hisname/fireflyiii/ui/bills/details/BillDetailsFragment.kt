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

package xyz.hisname.fireflyiii.ui.bills.details

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.CalendarDayBinding
import xyz.hisname.fireflyiii.databinding.DetailsCardBinding
import xyz.hisname.fireflyiii.databinding.FragmentBillDetailsBinding
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.ui.bills.AddBillFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionSeparatorAdapter
import xyz.hisname.fireflyiii.ui.transaction.details.TransactionDetailsFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel
import xyz.hisname.fireflyiii.util.openFile
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class BillDetailsFragment: BaseDetailFragment() {

    private val selectedPayDays = arrayListOf<LocalDate>()
    private val selectedPaidDays = arrayListOf<LocalDate>()
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val billDetailsViewModel by lazy { getImprovedViewModel(BillDetailsViewModel::class.java) }
    private val billId: Long by lazy { arguments?.getLong("billId") ?: 0  }
    private val transactionAdapter by lazy { TransactionSeparatorAdapter{ data -> itemClicked(data) } }
    private var selectedDate: LocalDate? = null
    private var fragmentBillDetailBinding: FragmentBillDetailsBinding? = null
    private val binding get() = fragmentBillDetailBinding!!
    private var detailsCardBinding: DetailsCardBinding? = null
    private val detailBinding get() = detailsCardBinding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBillDetailBinding = FragmentBillDetailsBinding.inflate(inflater, container, false)
        detailsCardBinding = binding.billInfoCard
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billDetailsViewModel.billId = billId
        setBillInfo()
        setPayCalendar()
        setPaidCalendar()
        setCalendarWidgets(binding.payDatesCalendarView)
        setCalendarWidgets(binding.paidDatesCalendarView)
        getPayDates(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
        getPaidDates(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
        progressCircle()
        //setReminder()
        binding.transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.transactionRecyclerView.adapter = transactionAdapter
    }

    private fun setBillInfo(){
        billDetailsViewModel.getBillInfo().observe(viewLifecycleOwner){ billData ->
            val attributes = billData.billAttributes
            val bill = arrayListOf(
                    DetailModel(resources.getString(R.string.name), attributes.name),
                    DetailModel("Range", attributes.currency_symbol +
                            attributes.amount_min + " ~ " + attributes.amount_max),
                    DetailModel(resources.getString(R.string.frequency), attributes.repeat_freq),
                    DetailModel("Is Active", attributes.active.toString())
            )
            if(attributes.notes.isNullOrEmpty()){
                binding.notesCard.isGone = true
            } else {
                binding.notesText.text = attributes.notes?.toMarkDown()
            }
            downloadAttachment()
            detailBinding.detailsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            detailBinding.detailsRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            detailBinding.detailsRecyclerView.adapter = BaseDetailRecyclerAdapter(bill){ }
        }
    }

    private fun getPayDates(startDate: String, endDate: String){
        billDetailsViewModel.getPayList(startDate, endDate).observe(viewLifecycleOwner){ billPayList ->
            billPayList.forEach {  billPayDates ->
                selectedPayDays.add(billPayDates.payDates)
            }
            binding.payDatesCalendarView.notifyCalendarChanged()
        }
    }

    private fun getPaidDates(startDate: String, endDate: String){
        billDetailsViewModel.getPaidList(startDate, endDate).observe(viewLifecycleOwner){ billPaidList ->
            billPaidList.forEach {  billPaidDates ->
                selectedPaidDays.add(billPaidDates.date)
            }
            binding.paidDatesCalendarView.notifyCalendarChanged()
        }
    }

    private fun setPayCalendar(){
        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay
            val onDayText = CalendarDayBinding.bind(view).dayText
        }

        binding.payDatesCalendarView.dayBinder = object: MonthDayBinder<DayViewContainer>{
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.onDayText
                textView.text = data.date.dayOfMonth.toString()
                if (data.position == DayPosition.MonthDate) {
                    if (selectedPayDays.isNotEmpty()){
                        selectedPayDays.forEach { day ->
                            if(day == data.date){
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
        binding.payDatesCalendarView.monthScrollListener = { month ->
            getPayDates(month.yearMonth.atDay(1).toString(), month.yearMonth.atEndOfMonth().toString())
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            binding.payDatesHeaderText.text = title
        }

    }

    private fun getPaidTransactions(date: LocalDate){
        billDetailsViewModel.getPaidTransactions(date).observe(viewLifecycleOwner){ transactions ->
            transactionAdapter.submitData(lifecycle, transactions)
        }
    }


    private fun setPaidCalendar(){
        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay
            val onDayText = CalendarDayBinding.bind(view).dayText
            val legendDivider = CalendarDayBinding.bind(view).legendDivider
            init {
                view.setOnClickListener {
                    if (selectedPaidDays.contains(day.date)){
                        selectedDate = day.date
                        getPaidTransactions(day.date)
                    }
                    binding.paidDatesCalendarView.notifyCalendarChanged()
                }
            }
        }

        binding.paidDatesCalendarView.dayBinder = object: MonthDayBinder<DayViewContainer>{
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.onDayText
                val divider = container.legendDivider
                textView.text = data.date.dayOfMonth.toString()
                if (data.position == DayPosition.MonthDate) {
                    if (selectedPaidDays.isNotEmpty()){
                        selectedPaidDays.forEach { day ->
                            if(day == data.date){
                                divider.setBackgroundColor(getCompatColor(R.color.md_green_500))
                            }
                        }
                    }
                    if(selectedDate == data.date){
                        textView.setBackgroundColor(getCompatColor(R.color.md_green_500))
                    }
                    textView.setTextColor(setDayNightTheme())
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
        binding.paidDatesCalendarView.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            binding.paidDatesHeaderText.text = title
            getPaidDates(month.yearMonth.atDay(1).toString(), month.yearMonth.atEndOfMonth().toString())
        }
    }

    private fun setCalendarWidgets(calendarView: CalendarView){
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusYears(40)
        val endMonth = currentMonth.plusYears(30)
        calendarView.setup(startMonth, endMonth,
                WeekFields.of(Locale.getDefault()).firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
        calendarView.updateMonthData()
    }

    private fun progressCircle(){
        billDetailsViewModel.isLoading.observe(viewLifecycleOwner){ isloading ->
            if(isloading){
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(binding.progressLayout.progressOverlay, View.GONE, 0f, 200)
            }
        }
        transactionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            if(loadStates.refresh !is LoadState.Loading) {
                binding.transactionLoader.hide()
            } else {
                binding.transactionLoader.show()
            }
        }
    }

    private fun setDownloadClickListener(attachmentData: AttachmentData, attachmentAdapter: ArrayList<AttachmentData>){
        billDetailsViewModel.downloadAttachment(attachmentData).observe(viewLifecycleOwner) { downloadedFile ->
            // "Refresh" the icon. From downloading to open file
            binding.attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentAdapter,
                    true, { data: AttachmentData ->
                setDownloadClickListener(data, attachmentDataAdapter)
                    }){ another: Int -> }
            startActivity(requireContext().openFile(downloadedFile))
        }
    }

    private fun downloadAttachment(){
        billDetailsViewModel.billAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        true, { data: AttachmentData ->
                    setDownloadClickListener(data, attachmentDataAdapter)
                }) { another: Int -> }
            }
        }
    }

    private fun itemClicked(data: Transactions){
        parentFragmentManager.commit {
            add(R.id.fragment_container, TransactionDetailsFragment().apply {
                arguments = bundleOf("transactionJournalId" to data.transaction_journal_id)
            })
            addToBackStack(null)
        }
    }

    private fun setDayNightTheme(): Int{
        return if(globalViewModel.isDark){
            getCompatColor(R.color.md_white_1000)
        } else {
            getCompatColor(R.color.md_black_1000)
        }
    }

    // TODO: 14 Jan 2022
    // This needs a lot more testing. Alarms on Android is a mess.
    // Alternative to Alarm Manager is using Work Manager but there is no guarantee that the work
    // will be fired.
    // We shall defer enabling in production
    private fun setReminder(){
        binding.alarmCheckbox.setOnClickListener {
            val alarmManager = requireActivity().getSystemService(AlarmManager::class.java) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(alarmManager.canScheduleExactAlarms()){
                    billDetailsViewModel.setBillReminder(alarmManager, binding.alarmCheckbox.isChecked)
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("Devices running Android S onwards requires a special Alarm permission. " +
                                "Please enable it in settings")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            val intent = Intent()
                            intent.action = ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            requireActivity().startActivity(intent)
                        }
                        .show()
                }
            } else {
                billDetailsViewModel.setBillReminder(alarmManager, binding.alarmCheckbox.isChecked)
            }
        }

    }

    override fun deleteItem() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_bill_title, billDetailsViewModel.billName))
                .setMessage(resources.getString(R.string.delete_bill_message, billDetailsViewModel.billName))
                .setPositiveButton(R.string.delete_permanently) { _, _ ->
                    billDetailsViewModel.deleteBill().observe(viewLifecycleOwner) { isDeleted ->
                        if(isDeleted){
                            parentFragmentManager.popBackStack()
                            toastSuccess(resources.getString(R.string.bill_deleted, billDetailsViewModel.billName))
                        } else {
                            toastOffline(getString(R.string.generic_delete_error))
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }

    override fun editItem() {
        parentFragmentManager.commit{
            replace(R.id.bigger_fragment_container, AddBillFragment().apply{
                arguments = bundleOf("billId" to billId)
            })
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBillDetailBinding = null
        detailsCardBinding = null
    }

}