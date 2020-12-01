package xyz.hisname.fireflyiii.ui.settings

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.repository.DestroyItemsViewModel
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class DeleteItemsFragment: BaseSettings() {

    private val destroyItemsViewModel by lazy { getImprovedViewModel(DestroyItemsViewModel::class.java) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.delete_items_settings)
        handleFinancialControl()
        handleAccounts()
        handleAutomation()
        handleTransaction()
        handleClassification()
        destroyItemsViewModel.message.observe(this){ message ->
            toastInfo(message)
        }
    }

    private fun handleFinancialControl(){
        val deleteBudget = findPreference<Preference>("delete_budget") as Preference
        deleteBudget.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_chart_pie).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteBudget.setOnPreferenceClickListener {
            askUserConfirmation("budget")
            true
        }
        val deleteBills = findPreference<Preference>("delete_bills") as Preference
        deleteBills.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_calendar).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }

        deleteBills.setOnPreferenceClickListener {
            askUserConfirmation("bills")
            true
        }
        val piggyBanks = findPreference<Preference>("delete_piggy_bank") as Preference
        piggyBanks.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_piggy_bank).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        piggyBanks.setOnPreferenceClickListener {
            askUserConfirmation("piggy_banks")
            true
        }
    }

    private fun handleAccounts(){
        val deleteAllAccounts = findPreference<Preference>("delete_all_accounts") as Preference
        deleteAllAccounts.icon = IconicsDrawable(requireContext(), GoogleMaterial.Icon.gmd_credit_card).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteAllAccounts.setOnPreferenceClickListener {
            askUserConfirmation("accounts")
            true
        }
        val deleteAsset = findPreference<Preference>("delete_assets") as Preference
        deleteAsset.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_money_bill).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteAsset.setOnPreferenceClickListener {
            askUserConfirmation("asset_accounts")
            true
        }
        val deleteExpense = findPreference<Preference>("delete_expense") as Preference
        deleteExpense.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_shopping_cart).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteExpense.setOnPreferenceClickListener {
            askUserConfirmation("expense_accounts")
            true
        }
        val deleteRevenue = findPreference<Preference>("delete_revenue") as Preference
        deleteRevenue.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_download).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteRevenue.setOnPreferenceClickListener {
            askUserConfirmation("revenue_accounts")
            true
        }
        val deleteLiabilities = findPreference<Preference>("delete_liabilities") as Preference
        deleteLiabilities.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_ticket_alt).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteLiabilities.setOnPreferenceClickListener {
            askUserConfirmation("liabilities")
            true
        }
    }

    private fun handleTransaction(){
        val deleteTransaction = findPreference<Preference>("delete_transaction") as Preference
        deleteTransaction.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_exchange_alt).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteTransaction.setOnPreferenceClickListener {
            askUserConfirmation("transactions")
            true
        }
        val deleteWithdrawals = findPreference<Preference>("delete_withdrawals") as Preference
        deleteWithdrawals.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_long_arrow_alt_left).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteWithdrawals.setOnPreferenceClickListener {
            askUserConfirmation("withdrawals")
            true
        }
        val deleteDeposits = findPreference<Preference>("delete_deposits") as Preference
        deleteDeposits.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_long_arrow_alt_right).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteDeposits.setOnPreferenceClickListener {
            askUserConfirmation("deposits")
            true
        }
        val deleteTransfer = findPreference<Preference>("delete_transfers") as Preference
        deleteTransfer.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_exchange_alt).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteTransfer.setOnPreferenceClickListener {
            askUserConfirmation("transfers")
            true
        }
    }

    private fun handleAutomation(){
        val deleteRules = findPreference<Preference>("delete_rules") as Preference
        deleteRules.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_random).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteRules.setOnPreferenceClickListener {
            askUserConfirmation("rules")
            true
        }
        val deleteRecurringTransaction = findPreference<Preference>("delete_recurring_transactions") as Preference
        deleteRecurringTransaction.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_paint_brush).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteRecurringTransaction.setOnPreferenceClickListener {
            askUserConfirmation("recurring")
            true
        }
    }

    private fun handleClassification(){
        val deleteCategories = findPreference<Preference>("delete_categories") as Preference
        deleteCategories.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_exclamation_triangle).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteCategories.setOnPreferenceClickListener {
            askUserConfirmation("categories")
            true
        }
        val deleteTags = findPreference<Preference>("delete_tags") as Preference
        deleteTags.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_tags).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteTags.setOnPreferenceClickListener {
            askUserConfirmation("tags")
            true
        }
        val deleteObjectGroups = findPreference<Preference>("delete_object_group") as Preference
        deleteObjectGroups.icon = IconicsDrawable(requireContext(), FontAwesome.Icon.faw_envelope).apply {
            colorRes = setIconColor()
            sizeDp = 24
        }
        deleteObjectGroups.setOnPreferenceClickListener {
            askUserConfirmation("object_groups")
            true
        }
    }

    private fun askUserConfirmation(itemToDelete: String){
        val random = randomGenerator()
        val spannableString = SpannableString("This action cannot be undone. Please type $random to confirm")
        val redColor = ColorStateList(arrayOf(intArrayOf()), intArrayOf(getCompatColor(R.color.md_red_600)))
        val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, redColor, null)
        spannableString.setSpan(highlightSpan, 42, 49, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val input = EditText(requireContext())
        val linearLayout = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = linearLayout
        AlertDialog.Builder(requireContext())
                .setTitle(requireContext().getString(R.string.are_you_sure))
                .setMessage(spannableString)
                .setView(input)
                .setPositiveButton(requireContext().getString(R.string.user_understand)){ _, _ ->
                    if(input.getString().contentEquals(random)){
                        destroyItemsViewModel.deleteObject(itemToDelete)
                    } else {
                        toastInfo("Input does not match!", Toast.LENGTH_LONG)
                    }
                }
                .show()
    }


    private fun randomGenerator() : String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..7)
                .map{ kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
    }

    private fun setIconColor(): Int{
        return if(globalViewModel.isDark){
            R.color.md_white_1000
        } else {
            R.color.md_black_1000
        }
    }

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}