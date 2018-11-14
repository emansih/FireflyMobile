package xyz.hisname.fireflyiii.ui.account

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_add_account.*
import xyz.hisname.fireflyiii.repository.models.accounts.AccountAttributes
import xyz.hisname.fireflyiii.util.extension.getString
import java.util.*

class UpdateAccountFragment: BaseAccountFragment(){

    private var accountAttributes: AccountAttributes? = null
    private val accountId: Long by lazy { arguments?.getLong("accountId") as Long  }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountAttributes = Gson().fromJson(requireActivity().intent.getSerializableExtra("accountData").toString(),
                AccountAttributes::class.java)
        setWidget()
        accountType.isVisible = false
        if(Objects.equals(accountArgument, "Asset Account")){
            accountRole.isVisible = true
        } else if(Objects.equals(accountArgument,"Liability Account")) {
            liabilityType.isVisible = true
            liabilityAmount.isVisible = true
            liabilityStartDate.isVisible = true
            liabilityInterest.isVisible = true
            interestPeriod.isVisible = true
        }
    }

    private fun setWidget(){
        accountName.setText(accountAttributes?.name)
        // TODO: Show a more detailed currency format in the edit text
        currencyCode.setText(accountAttributes?.currency_code)
        currency = accountAttributes?.currency_code.toString()
        accountNumber.setText(accountAttributes?.account_number)
        if(accountAttributes?.credit_card_type != null){
            ccType.isVisible = true
            ccType.setText(accountAttributes?.credit_card_type)
            ccPaymentDate.isVisible = true
            ccPaymentDate.setText(accountAttributes?.monthly_payment_date)
        }

    }

    override fun submitData() {
        val liability_type = if(liabilityType.isVisible) {
            when {
                liabilityType.selectedItemPosition == 0 -> "loan"
                liabilityType.selectedItemPosition == 1 -> "debt"
                liabilityType.selectedItemPosition == 2 -> "mortgage"
                else -> "credit card"
            }
        } else {
            null
        }
        val liability_amount = if(liabilityAmount.isVisible){
            liabilityAmount.getString()
        } else {
            null
        }
        val liability_start_date = if(liabilityStartDate.isVisible){
            liabilityStartDate.getString()
        } else {
            null
        }
        val liability_interest = if(liabilityInterest.isVisible) {
            liabilityInterest.getString()
        } else {
            null
        }
        val interest_period= if(interestPeriod.isVisible) {
            when {
                interestPeriod.selectedItemPosition == 0 -> "daily"
                interestPeriod.selectedItemPosition == 1 -> "monthly"
                else -> "yearly"
            }
        } else {
            null
        }
        val creditCardType = if(ccType.isVisible){
            ccType.getString()
        } else {
            null
        }
        val creditCardDate = if(ccPaymentDate.isVisible){
            ccPaymentDate.getString()
        } else {
            null
        }
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = "Edit $accountArgument"
    }

}