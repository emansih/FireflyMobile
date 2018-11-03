package xyz.hisname.fireflyiii.repository.models

import xyz.hisname.fireflyiii.repository.models.transaction.TransactionData

data class ReportPair(val first: MutableList<TransactionData>?, val second: MutableList<TransactionData>?)