package xyz.hisname.fireflyiii.ui.bills

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color.rgb
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.fragment_add_bill.*
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.receiver.BillReceiver
import xyz.hisname.fireflyiii.repository.models.bills.BillAttributes
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyListBottomSheet
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.DialogDarkMode
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*

class AddBillFragment: BaseAddObjectFragment() {

    private var billAttribute: BillAttributes? = null
    private var notes: String? = null
    private var repeatFreq: String = ""
    private var currency = ""
    private val billId by lazy { arguments?.getLong("billId") ?: 0 }
    private lateinit var freqAdapter: ArrayAdapter<String>
    private var billDescription: String? = ""
    private lateinit var queue: FancyShowCaseQueue

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_bill, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_bill_layout)
        if(billId != 0L){
            billViewModel.getBillById(billId).observe(this, Observer {
                billAttribute = it[0].billAttributes
                description_edittext.setText(billAttribute?.name)
                billDescription = billAttribute?.name
                min_amount_edittext.setText(billAttribute?.amount_min.toString())
                max_amount_edittext.setText(billAttribute?.amount_max.toString())
                currency = billAttribute?.currency_code ?: ""
                currencyViewModel.getCurrencyByCode(currency).observe(this, Observer { currencyList ->
                    val currencyData = currencyList[0].currencyAttributes
                    currency_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
                })
                bill_date_edittext.setText(billAttribute?.date)
                skip_edittext.setText(billAttribute?.skip.toString())
                notes_edittext.setText(billAttribute?.notes)
                val spinnerPosition = freqAdapter.getPosition(
                        billAttribute?.repeat_freq?.substring(0, 1)?.toUpperCase() +
                                billAttribute?.repeat_freq?.substring(1))
                frequency_spinner.setSelection(spinnerPosition)
            })
        }
        showHelpText()
    }

    override fun onStart() {
        super.onStart()
        addBillFab.setOnClickListener {
            hideKeyboard()
            ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            notes = if(notes_edittext.isBlank()){
                null
            } else {
                notes_edittext.getString()
            }
            if(billId == 0L){
                submitData()
            } else {
                updateBill()
            }
        }
    }

    override fun setIcons(){
        currency_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_money_bill)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_green_400))
                        .sizeDp(24),null, null, null)
        min_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_dollar_sign)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_yellow_A700))
                        .sizeDp(16),null, null, null)
        max_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_dollar_sign)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_yellow_A700))
                        .sizeDp(16),null, null, null)
        bill_date_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext())
                .icon(FontAwesome.Icon.faw_calendar)
                .color(ColorStateList.valueOf(rgb(18, 122, 190)))
                .sizeDp(24),null, null, null)
        skip_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_sort_numeric_up)
                        .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                        .sizeDp(24),null, null, null)
        addBillFab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
        addBillFab.setImageDrawable(IconicsDrawable(requireContext()).icon(FontAwesome.Icon.faw_save)
                .color(ContextCompat.getColor(requireContext(), R.color.md_black_1000))
                .sizeDp(24))
        if(billId != 0L) {
            placeHolderToolbar.inflateMenu(R.menu.delete_menu)
            placeHolderToolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.menu_item_delete) {
                    val deleteBill = DeleteBillDialog()
                    deleteBill.arguments = bundleOf("billId" to billId, "billDescription" to billDescription)
                    deleteBill.show(requireFragmentManager().beginTransaction(), "delete_bill_dialog")
                }
                true
            }
        }
    }

    private fun showHelpText(){
        val addBillDescriptionCaseView = FancyShowCaseView.Builder(requireActivity())
                .focusOn(appbar)
                .title(resources.getString(R.string.bills_create_intro))
                .enableAutoTextPosition()
                .fitSystemWindows(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .showOnce("addBillDescriptionCaseView")
                .closeOnTouch(true)
                .build()

        val descriptionCaseView = FancyShowCaseView.Builder(requireActivity())
                .focusOn(description_edittext)
                .title(resources.getString(R.string.bills_create_name))
                .closeOnTouch(true)
                .enableAutoTextPosition()
                .fitSystemWindows(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .showOnce("descriptionCaseView")
                .build()

        val minMaxAmountCaseView = FancyShowCaseView.Builder(requireActivity())
                .focusOn(min_amount_layout)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .enableAutoTextPosition()
                .title(resources.getString(R.string.bills_create_amount_min_holder))
                .closeOnTouch(true)
                .fitSystemWindows(true)
                .showOnce("minMaxAmountCaseView")
                .build()

        val freqCaseView = FancyShowCaseView.Builder(requireActivity())
                .focusOn(frequency_spinner)
                .title(resources.getString(R.string.bills_create_repeat_freq_holder))
                .closeOnTouch(true)
                .enableAutoTextPosition()
                .fitSystemWindows(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .showOnce("freqCaseView")
                .build()

        val skipCaseView = FancyShowCaseView.Builder(requireActivity())
                .focusOn(skip_layout)
                .title(resources.getString(R.string.bills_create_skip_holder))
                .closeOnTouch(true)
                .enableAutoTextPosition()
                .fitSystemWindows(true)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .showOnce("skipCaseView")
                .build()

        queue = FancyShowCaseQueue()
                .add(addBillDescriptionCaseView)
                .add(descriptionCaseView)
                .add(minMaxAmountCaseView)
                .add(skipCaseView)
                .add(freqCaseView)
        queue.show()
    }

    override fun setWidgets(){
        frequency_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.selectedItemId
                repeatFreq = when (selectedItem) {
                    0L -> "weekly"
                    1L -> "monthly"
                    2L -> "quarterly"
                    3L -> "half-yearly"
                    4L -> "yearly"
                    else -> ""
                }
            }
        }
        val calendar = Calendar.getInstance()
        val startDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                bill_date_edittext.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }
        bill_date_edittext.setOnClickListener {
            hideKeyboard()
            DialogDarkMode().showCorrectDatePickerDialog(requireContext(), startDate, calendar)
        }
        currency_edittext.setOnClickListener{
            CurrencyListBottomSheet().show(requireFragmentManager(), "currencyList" )
        }
        currencyViewModel.currencyCode.observe(this, Observer {
            currency = it
        })

        currencyViewModel.currencyDetails.observe(this, Observer {
            currency_edittext.setText(it)
        })
        placeHolderToolbar.setNavigationOnClickListener{ handleBack() }
        currencyViewModel.getDefaultCurrency().observe(this, Observer { defaultCurrency ->
            val currencyData = defaultCurrency[0].currencyAttributes
            currency_edittext.setText(currencyData?.name + " (" + currencyData?.code + ")")
            currency = currencyData?.code.toString()
        })
    }

    override fun submitData(){
        billViewModel.addBill(description_edittext.getString(),
                min_amount_edittext.getString(), max_amount_edittext.getString(),
                bill_date_edittext.getString(), repeatFreq, skip_edittext.getString(), "1",
                    currency, notes)
                .observe(this, Observer { response ->
                    ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
                    val errorMessage = response.getErrorMessage()
                    if (errorMessage != null) {
                        toastError(errorMessage)
                    } else if (response.getError() != null) {
                        val billBroadcast = Intent(requireContext(), BillReceiver::class.java).apply {
                            action = "firefly.hisname.ADD_BILL"
                        }
                        val extras = bundleOf(
                                "name" to description_edittext.getString(),
                                "minAmount" to min_amount_edittext.getString(),
                                "maxAmount" to max_amount_edittext.getString(),
                                "billDate" to bill_date_edittext.getString(),
                                "repeatFreq" to repeatFreq,
                                "skip" to skip_edittext.getString(),
                                "currencyCode" to currency,
                                "notes" to notes)
                        billBroadcast.putExtras(extras)
                        requireActivity().sendBroadcast(billBroadcast)
                        toastOffline(getString(R.string.data_added_when_user_online, "Bill"))
                        handleBack()
                    } else if (response.getResponse() != null) {
                        toastSuccess("Bill saved")
                        handleBack()
                    }
                })
    }

    private fun updateBill(){
        billViewModel.updateBill(billId, description_edittext.getString(),
                min_amount_edittext.getString(), max_amount_edittext.getString(),
                bill_date_edittext.getString(), repeatFreq, skip_edittext.getString(), "1",
                currency, notes).observe(this, Observer { response ->
            ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            if (response.getErrorMessage() != null) {
                toastError(response.getErrorMessage())
            } else if (response.getError() != null) {
                toastError(response.getError()?.localizedMessage)
            } else if (response.getResponse() != null) {
                toastSuccess("Bill updated")
                handleBack()
            }
        })
    }

    override fun handleBack() {
        unReveal(dialog_add_bill_layout)
    }

}