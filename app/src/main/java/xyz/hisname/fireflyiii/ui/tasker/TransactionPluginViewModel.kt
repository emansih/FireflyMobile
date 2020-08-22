package xyz.hisname.fireflyiii.ui.tasker

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TransactionPluginViewModel: ViewModel(){

    val transactionType: MutableLiveData<String> = MutableLiveData()
    val transactionDescription: MutableLiveData<String> = MutableLiveData()
    val transactionDateTime: MutableLiveData<String> = MutableLiveData()
    val transactionPiggyBank: MutableLiveData<String?> = MutableLiveData()
    val transactionAmount: MutableLiveData<String> = MutableLiveData()
    val transactionSourceAccount: MutableLiveData<String> = MutableLiveData()
    val transactionDestinationAccount: MutableLiveData<String> = MutableLiveData()
    val transactionCurrency: MutableLiveData<String> = MutableLiveData()
    val transactionCategory: MutableLiveData<String> = MutableLiveData()
    val transactionTags: MutableLiveData<String?> = MutableLiveData()
    val transactionBudget: MutableLiveData<String> = MutableLiveData()
    val fileUri: MutableLiveData<Uri?> = MutableLiveData()
    val removeFragment: MutableLiveData<Boolean> = MutableLiveData()
}