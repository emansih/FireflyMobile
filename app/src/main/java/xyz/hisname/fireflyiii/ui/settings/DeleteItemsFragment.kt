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
import androidx.lifecycle.observe
import androidx.preference.Preference
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.DestroyItemsViewModel
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.extension.getImprovedViewModel

class DeleteItemsFragment: BaseSettings() {

    private val destroyItemsViewModel by lazy { getImprovedViewModel(DestroyItemsViewModel::class.java) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.delete_items_settings)
        handleFinancialControl()
        handleAccounts()
        handleTransaction()
        destroyItemsViewModel.message.observe(this){ message ->
            toastInfo(message)
        }
    }

    private fun handleFinancialControl(){
        val deleteBudget = findPreference<Preference>("delete_budget") as Preference
        deleteBudget.setOnPreferenceClickListener {
            askUserConfirmation("budget")
            true
        }
        val deleteBills = findPreference<Preference>("delete_bills") as Preference
        deleteBills.setOnPreferenceClickListener {
            askUserConfirmation("bills")
            true
        }
        val piggyBanks = findPreference<Preference>("delete_piggy_bank") as Preference
        piggyBanks.setOnPreferenceClickListener {
            askUserConfirmation("piggy_banks")
            true
        }
    }

    private fun handleAccounts(){
        val deleteAllAccounts = findPreference<Preference>("delete_all_accounts") as Preference
        deleteAllAccounts.setOnPreferenceClickListener {
            askUserConfirmation("accounts")
            true
        }
        val deleteAsset = findPreference<Preference>("delete_assets") as Preference
        deleteAsset.setOnPreferenceClickListener {
            askUserConfirmation("asset_accounts")
            true
        }
        val deleteExpense = findPreference<Preference>("delete_expense") as Preference
        deleteExpense.setOnPreferenceClickListener {
            askUserConfirmation("expense_accounts")
            true
        }
        val deleteRevenue = findPreference<Preference>("delete_revenue") as Preference
        deleteRevenue.setOnPreferenceClickListener {
            askUserConfirmation("revenue_accounts")
            true
        }
        val deleteLiabilities = findPreference<Preference>("delete_liabilities") as Preference
        deleteLiabilities.setOnPreferenceClickListener {
            askUserConfirmation("liabilities")
            true
        }
    }

    private fun handleTransaction(){
        val deleteTransaction = findPreference<Preference>("delete_transaction") as Preference
        deleteTransaction.setOnPreferenceClickListener {
            askUserConfirmation("transactions")
            true
        }
        val deleteWithdrawals = findPreference<Preference>("delete_withdrawals") as Preference
        deleteWithdrawals.setOnPreferenceClickListener {
            askUserConfirmation("withdrawals")
            true
        }
        val deleteDeposits = findPreference<Preference>("delete_deposits") as Preference
        deleteDeposits.setOnPreferenceClickListener {
            askUserConfirmation("deposits")
            true
        }
        val deleteTransfer = findPreference<Preference>("delete_transfers") as Preference
        deleteTransfer.setOnPreferenceClickListener {
            askUserConfirmation("transfers")
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
                .setTitle("Are you absolutely sure?")
                .setMessage(spannableString)
                .setView(input)
                .setPositiveButton("I understand the consequences"){ _, _ ->
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

    override fun handleBack() {
        parentFragmentManager.popBackStack()
    }
}