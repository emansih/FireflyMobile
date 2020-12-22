package xyz.hisname.fireflyiii.ui.bills.details

import android.content.Context
import android.os.Bundle
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
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.details_card.*
import kotlinx.android.synthetic.main.fragment_bill_details.*
import kotlinx.android.synthetic.main.fragment_bill_details.notesCard
import kotlinx.android.synthetic.main.fragment_bill_details.notesText
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.repository.models.transaction.Transactions
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.ui.bills.AddBillFragment
import xyz.hisname.fireflyiii.ui.transaction.TransactionAdapter
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
    private val transactionAdapter by lazy { TransactionAdapter{ data -> itemClicked(data) } }
    private var selectedDate: LocalDate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_bill_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billDetailsViewModel.billId = billId
        setBillInfo()
        setPayCalendar()
        setPaidCalendar()
        setCalendarWidgets(payDatesCalendarView)
        setCalendarWidgets(paidDatesCalendarView)
        getPayDates(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
        getPaidDates(DateTimeUtil.getStartOfMonth(), DateTimeUtil.getEndOfMonth())
        progressCircle()
        transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        transactionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        transactionRecyclerView.adapter = transactionAdapter
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
                notesCard.isGone = true
            } else {
                notesText.text = attributes.notes?.toMarkDown()
            }
            downloadAttachment()
            detailsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            detailsRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            detailsRecyclerView.adapter = BaseDetailRecyclerAdapter(bill){ }
        }
    }

    private fun getPayDates(startDate: String, endDate: String){
        billDetailsViewModel.getPayList(startDate, endDate).observe(viewLifecycleOwner){ billPayList ->
            billPayList.forEach {  billPayDates ->
                selectedPayDays.add(billPayDates.payDates)
            }
            payDatesCalendarView.notifyCalendarChanged()
        }
    }

    private fun getPaidDates(startDate: String, endDate: String){
        billDetailsViewModel.getPaidList(startDate, endDate).observe(viewLifecycleOwner){ billPaidList ->
            billPaidList.forEach {  billPaidDates ->
                selectedPaidDays.add(billPaidDates.date)
            }
            paidDatesCalendarView.notifyCalendarChanged()
        }
    }

    private fun setPayCalendar(){
        class DayViewContainer(view: View): ViewContainer(view) {
            lateinit var day: CalendarDay
            val onDayText = view.dayText
        }

        payDatesCalendarView.dayBinder = object: DayBinder<DayViewContainer>{
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.onDayText
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    if (selectedPayDays.isNotEmpty()){
                        selectedPayDays.forEach { data ->
                            if(data == day.date){
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
        payDatesCalendarView.monthScrollListener = { month ->
            getPayDates(month.yearMonth.atDay(1).toString(), month.yearMonth.atEndOfMonth().toString())
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            payDatesHeaderText.text = title
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
            val onDayText = view.dayText
            val legendDivider = view.legendDivider
            init {
                view.setOnClickListener {
                    if (selectedPaidDays.contains(day.date)){
                        selectedDate = day.date
                        getPaidTransactions(day.date)
                    }
                    paidDatesCalendarView.notifyCalendarChanged()
                }
            }
        }

        paidDatesCalendarView.dayBinder = object: DayBinder<DayViewContainer>{
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.onDayText
                val divider = container.legendDivider
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    if (selectedPaidDays.isNotEmpty()){
                        selectedPaidDays.forEach { data ->
                            if(data == day.date){
                                divider.setBackgroundColor(getCompatColor(R.color.md_green_500))
                            }
                        }
                    }
                    if(selectedDate == day.date){
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
        paidDatesCalendarView.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            paidDatesHeaderText.text = title
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
        calendarView.updateMonthConfiguration()
    }

    private fun progressCircle(){
        billDetailsViewModel.isLoading.observe(viewLifecycleOwner){ isloading ->
            if(isloading){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
        transactionAdapter.loadStateFlow.asLiveData().observe(viewLifecycleOwner){ loadStates ->
            if(loadStates.refresh !is LoadState.Loading) {
                transactionLoader.hide()
            } else {
                transactionLoader.show()
            }
        }
    }

    private fun setDownloadClickListener(attachmentData: AttachmentData, attachmentAdapter: ArrayList<AttachmentData>){
        billDetailsViewModel.downloadAttachment(attachmentData).observe(viewLifecycleOwner) { downloadedFile ->
            // "Refresh" the icon. From downloading to open file
            attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentAdapter,
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
                attachmentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                attachmentRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.details)
    }

}