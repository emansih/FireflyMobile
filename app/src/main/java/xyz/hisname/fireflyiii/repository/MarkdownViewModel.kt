package xyz.hisname.fireflyiii.repository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MarkdownViewModel: ViewModel() {

    val markdownText =  MutableLiveData<String>()

}