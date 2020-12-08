package xyz.hisname.fireflyiii.ui.piggybank

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.commit
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.iconics.IconicsColor.Companion.colorList
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import kotlinx.android.synthetic.main.fragment_add_piggy.*
import kotlinx.android.synthetic.main.fragment_add_piggy.description_edittext
import kotlinx.android.synthetic.main.fragment_add_piggy.expansionLayout
import kotlinx.android.synthetic.main.fragment_add_piggy.placeHolderToolbar
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.MarkdownViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseAddObjectFragment
import xyz.hisname.fireflyiii.ui.markdown.MarkdownFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*

class AddPiggyFragment: BaseAddObjectFragment() {

    private var currentAmount: String? = null
    private var startDate: String? = null
    private var targetDate: String? = null
    private var notes: String? = null
    private val markdownViewModel by lazy { getViewModel(MarkdownViewModel::class.java) }
    private val piggyViewModel by lazy { getImprovedViewModel(AddPiggyViewModel::class.java) }
    private val piggyId by lazy { arguments?.getLong("piggyId") ?: 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_piggy, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReveal(dialog_add_piggy_layout)
        updateEditText()
        setFab()
        showHelpText()
    }

    private fun showHelpText() = showCase(R.string.piggy_bank_description_help_text,
                "descriptionCaseView", description_edittext).show()

    private fun updateEditText(){
        if(piggyId != 0L){
            piggyViewModel.getPiggyById(piggyId).observe(viewLifecycleOwner) { piggyData ->
                val piggyAttributes = piggyData.piggyAttributes
                description_edittext.setText(piggyAttributes?.name)
                target_amount_edittext.setText(piggyAttributes?.target_amount.toString())
                current_amount_edittext.setText(piggyAttributes?.current_amount.toString())
                date_started_edittext.setText(piggyAttributes?.start_date)
                date_target_edittext.setText(piggyAttributes?.target_date)
                note_edittext.setText(piggyAttributes?.notes)
                account_exposed_dropdown.setText(piggyData.piggyAttributes?.account_name)
            }
        }
    }

    private fun setAccordion(){
        expansionLayout.addListener { _, expanded ->
            if(expanded){
                showCase(R.string.piggy_bank_date_help_text,
                        "dateStartedCaseView", date_started_edittext).show()
            }
        }
    }

    private fun setFab(){
        if(piggyId != 0L){
            addPiggyFab.setImageDrawable(IconicsDrawable(requireContext()).icon(GoogleMaterial.Icon.gmd_update))
        }
        addPiggyFab.setOnClickListener {
            hideKeyboard()
            currentAmount = if (current_amount_edittext.isBlank()) {
                null
            } else {
                current_amount_edittext.getString()
            }
            startDate = if (date_started_edittext.isBlank()) {
                null
            } else {
                date_started_edittext.getString()
            }
            targetDate = if (date_target_edittext.isBlank()) {
                null
            } else {
                date_target_edittext.getString()
            }
            notes = if (note_edittext.isBlank()) {
                null
            } else {
                note_edittext.getString()
            }
            if(piggyId == 0L) {
                submitData()
            } else {
                updatePiggyBank()
            }
        }
    }

    override fun setIcons(){
        target_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        current_amount_edittext.setCompoundDrawablesWithIntrinsicBounds(
                IconicsDrawable(requireContext()).apply {
                    icon = FontAwesome.Icon.faw_money_bill
                    colorRes = R.color.md_green_400
                    sizeDp = 24
                },null, null, null)
        date_started_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        },null, null, null)
        date_target_edittext.setCompoundDrawablesWithIntrinsicBounds(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_calendar
            color = colorList(ColorStateList.valueOf(Color.rgb(18, 122, 190)))
            sizeDp = 24
        },null, null, null)
        addPiggyFab.setBackgroundColor(getCompatColor(R.color.colorPrimaryDark))
        addPiggyFab.setImageDrawable(IconicsDrawable(requireContext()).apply {
            icon = FontAwesome.Icon.faw_plus
            colorRes = R.color.md_black_1000
            sizeDp = 24
        })
    }

    override fun setWidgets(){
        setAccordion()
        date_target_edittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                date_target_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        date_started_edittext.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            val picker = materialDatePicker.build()
            picker.show(parentFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { time ->
                date_started_edittext.setText(DateTimeUtil.getCalToString(time.toString()))
            }
        }
        piggyViewModel.getAccount().observe(viewLifecycleOwner) { list ->
            val accountAdapter = ArrayAdapter(requireContext(), R.layout.cat_exposed_dropdown_popup_item, list)
            accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            account_exposed_dropdown.setAdapter(accountAdapter)
        }
        placeHolderToolbar.setNavigationOnClickListener {
            handleBack()
        }
        note_edittext.setOnClickListener {
            markdownViewModel.markdownText.postValue(note_edittext.getString())
            parentFragmentManager.commit {
                replace(R.id.bigger_fragment_container, MarkdownFragment())
                addToBackStack(null)
            }
        }
        markdownViewModel.markdownText.observe(viewLifecycleOwner){ markdownText ->
            note_edittext.setText(markdownText)
        }
        piggyViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(progressLayout, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(progressLayout, View.GONE, 0f, 200)
            }
        }
    }

    override fun submitData(){
        piggyViewModel.addPiggyBank(description_edittext.getString(),
                account_exposed_dropdown.getString(), currentAmount, notes, startDate,
                target_amount_edittext.getString(), targetDate).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }

    }

    private fun updatePiggyBank(){
        piggyViewModel.updatePiggyBank(piggyId, description_edittext.getString(), account_exposed_dropdown.getString(),
                currentAmount, notes, startDate, target_amount_edittext.getString(), targetDate).observe(viewLifecycleOwner) { response ->
            if(response.first){
                toastSuccess(response.second)
                handleBack()
            } else {
                toastInfo(response.second)
            }
        }
    }

    override fun handleBack() {
        unReveal(dialog_add_piggy_layout)
        markdownViewModel.markdownText.postValue("")
    }
}