package xyz.hisname.fireflyiii.ui.bills

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_bill_create.*
import org.threeten.bp.LocalDate
import xyz.hisname.fireflyiii.Constants
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.util.extension.create
import java.util.*


class AddBillDialog: DialogFragment() {

    private var baseUrl: String? = null
    private var accessToken: String? = null
    private var autoMatchValue: CharSequence? = null
  //  private var scheduleChipValue: CharSequence? = null
    private var activeValue: CharSequence? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window.attributes.windowAnimations = R.style.DialogAnimation
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.dialog_bill_create,container)
    }

    override fun onStart() {
        super.onStart()
        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseUrl = arguments?.getString("fireflyUrl")
        accessToken = arguments?.getString("access_token")
        initWidget()
        closeButt()
        saveButton.setOnClickListener{
            parseValues()
        }
    }

    private fun closeButt(){
        button_close.setOnClickListener{
            val billFrag = ListBillFragment()
            val bundle = bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
            billFrag.arguments = bundle
            if(bill_name_edittext.text.toString().isEmpty() or
                    bill_match_edittext.text.toString().isEmpty() or
                    amount_min_edittext.text.toString().isEmpty() or
                    amount_max_edittext.text.toString().isEmpty() or
                    bill_date.text.toString().isEmpty() or note_edittext.text.toString().isEmpty()){
                Toasty.info(requireContext(), "No information entered. Bill not saved")
                requireActivity().supportFragmentManager.beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, billFrag)
                        .commit()
            } else {
                AlertDialog.Builder(requireContext())
                        .setTitle("Discard Bill?")
                        .setMessage("Your bill will not be saved")
                        .setPositiveButton("Discard") { _, _ ->
                            requireActivity().supportFragmentManager.beginTransaction()
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .replace(R.id.fragment_container, billFrag)
                                    .commit()
                        }
                        .setNegativeButton("Cancel") { _, _ ->

                        }
                        .show()
            }
        }
    }

    private fun parseValues(){
        val billName = bill_name_edittext.text.toString()
        val billMatch = bill_match_edittext.text.toString()
        val amountMin = amount_min_edittext.text.toString()
        val amountMax = amount_max_edittext.text.toString()
        val billDate = bill_date.text.toString()
        val skip = skip_edittext.text.toString()
        val notes = note_edittext.text.toString()

        if(automatch_radiogroup.checkedRadioButtonId == -1){
            Toasty.info(requireContext(), "Please select an automatch value")
        } else {
            autoMatchValue = requireActivity()
                    .findViewById<AppCompatRadioButton>(automatch_radiogroup.checkedRadioButtonId).text
        }

        /*if(chips_group.checkedChipId == -1){
            Toasty.info(requireContext(), "Please choose your bill frequency")
        } else {
            scheduleChipValue = requireActivity().findViewById<Chip>(chips_group.checkedChipId).text
        }
*/
        if(active_radiogroup.checkedRadioButtonId == -1){
            Toasty.info(requireContext(), "Please select an active value")
        } else {
            activeValue = requireActivity()
                    .findViewById<AppCompatRadioButton>(active_radiogroup.checkedRadioButtonId).text
        }

        autoMatchValue = if(autoMatchValue == "Yes"){
            "1"
        } else {
            "0"
        }

        activeValue = if(activeValue == "Yes"){
            "1"
        } else {
            "0"
        }
       /* addBillPresenter?.createBills(billName, billMatch, amountMin, amountMax, billDate,
                "", skip.toInt(), autoMatchValue.toString().toInt(),
                activeValue.toString().toInt(), notes)*/
    }

    private fun initWidget(){
       // bill_date.setText(LocalDateTime.now().toLocalDate().toString())
        bill_date.setText(LocalDate.now().dayOfMonth.toString() + LocalDate.now().month.toString()
        + LocalDate.now().year.toString())
        val calendar = Calendar.getInstance()
        val startDate = DatePickerDialog.OnDateSetListener{
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                // Sets date to yyyy-MM-dd format
            }
        }
        bill_date.setOnClickListener{
            DatePickerDialog(requireContext(), startDate, calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
                    .show()
        }
        skip_edittext.setText("0")
        val spinnerValue = arrayListOf(Constants.WEEKLY, Constants.MONTHLY,
                Constants.QUARTERLY, Constants.HALF_YEARLY, Constants.YEARLY)
        val dataAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinnerValue)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeat_spinner.adapter = dataAdapter
    }


 /*   override fun parseResults(results: String) {
        println("results: " + results)
    }

    override fun parseError(exception: Exception) {
        println("exception: " + exception)
    }

    override fun unknownHost() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), resources.getString(R.string.unable_ping_server)).show()
    }

    override fun authorizationError() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), resources.getString(R.string.authorization_error)).show()
    }

    override fun httpTimeOut() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), resources.getString(R.string.timeout_connecting)).show()
    }

    override fun nameEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        bill_name_edittext.error = resources.getString(R.string.required_field)
    }

    override fun amountMinEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        amount_min_edittext.error = resources.getString(R.string.required_field)
    }

    override fun amountMaxEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        amount_max_edittext.error = resources.getString(R.string.required_field)
    }

    override fun amountMismatch() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "Max amount cannot be less than min amount").show()
        amount_max_edittext.error = "Less than min amount"
        amount_min_edittext.error = "More than max amount"
    }

    override fun dateEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        bill_date.error = resources.getString(R.string.required_field)
    }

    override fun dateFormatError() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        bill_date.error = "Date format error"
    }

    override fun repeatFreqEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "Please select bill frequency").show()
    }

    override fun repeatFreqInvalidFormat() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "repeatFreqInvalidFormat").show()
    }

    override fun skipEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        skip_edittext.error = resources.getString(R.string.required_field)
    }

    override fun skipInvalid() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "skipInvalid").show()
    }

    override fun autoMatchEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "Please select an automatch value").show()
    }

    override fun autoMatchInvalid() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "autoMatchInvalid").show()
    }

    override fun activeEmpty() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "Please select an active value").show()
    }

    override fun activeInvalid() {
        ProgressBar.animateView(progress_overlay, View.GONE, 0.toFloat(), 200)
        Toasty.error(requireContext(), "activeInvalid").show()

    }
*/
}