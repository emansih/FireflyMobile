package xyz.hisname.fireflyiii.repository

import xyz.hisname.fireflyiii.data.local.dao.*
import xyz.hisname.fireflyiii.data.remote.firefly.api.SearchService
import xyz.hisname.fireflyiii.data.remote.firefly.api.TransactionService
import xyz.hisname.fireflyiii.repository.models.search.SearchModelItem
import java.util.stream.Stream

class SearchRepository(private val transactionDao: TransactionDataDao,
                       private val tagsDataDao: TagsDataDao,
                       private val piggyDao: PiggyDataDao,
                       private val currencyDao: CurrencyDataDao,
                       private val budgetList: BudgetListDataDao,
                       private val budgetLimitDao: BudgetLimitDao,
                       private val billDao: BillDataDao,
                       private val accountDao: AccountsDataDao,
                       private val categoryDataDao: CategoryDataDao,
                       private val searchService: SearchService,
                       private val transactionService: TransactionService) {


    suspend fun searchEverywhere(query: String): List<SearchModelItem> {
        val combineArray = arrayListOf<SearchModelItem>()
        try {
            Stream.of(searchBudget(query), searchAccounts(query), searchCategory(query),
                searchPiggyBanks(query), searchTags(query), searchTransaction(query),
                searchCurrencies(query), searchBill(query)).forEach { searchModelItemList ->
                        combineArray.addAll(searchModelItemList)
            }
        } catch (exception: Exception){ }
        return combineArray.distinct()
    }

    private suspend fun searchBudget(query: String): List<SearchModelItem>{
        val budgetQueries = arrayListOf<SearchModelItem>()
        try {
            val webservice = searchService.searchBudgets(query)
            val body = webservice.body()
            if (body != null && webservice.isSuccessful){
                body.forEach { searchModelItem ->
                    // Currency parameter in budget is optional
                    val budgetCurrency = budgetLimitDao.getUniqueCurrencySymbolInSpentByBudgetId(searchModelItem.id)
                    if(budgetCurrency.isNotEmpty()){
                        budgetCurrency.forEach { currencySymbol ->
                            budgetQueries.add(SearchModelItem(searchModelItem.id,
                                searchModelItem.name, "Budget", currencySymbol))
                        }
                    } else {
                        budgetQueries.add(SearchModelItem(searchModelItem.id,
                            searchModelItem.name, "Budget", ""))
                    }
                }
            } else {
                val budgetList = budgetList.searchBudgetName("%$query%")
                budgetList.forEach { budgetListData ->
                    // Currency parameter in budget is optional
                    val budgetCurrency = budgetLimitDao.getUniqueCurrencySymbolInSpentByBudgetId(budgetListData.budgetListId)
                    if(budgetCurrency.isNotEmpty()){
                        budgetCurrency.forEach { currencySymbol ->
                            budgetQueries.add(SearchModelItem(budgetListData.budgetListId,
                                budgetListData.budgetListAttributes.name, "Budget", currencySymbol))
                        }
                    } else {
                        budgetQueries.add(SearchModelItem(budgetListData.budgetListId,
                            budgetListData.budgetListAttributes.name, "Budget", ""))
                    }
                }
            }
        } catch (exception: Exception){
            val budgetList = budgetList.searchBudgetName("%$query%")
            budgetList.forEach { budgetListData ->
                // Currency parameter in budget is optional
                val budgetCurrency = budgetLimitDao.getUniqueCurrencySymbolInSpentByBudgetId(budgetListData.budgetListId)
                if(budgetCurrency.isNotEmpty()){
                    budgetCurrency.forEach { currencySymbol ->
                        budgetQueries.add(SearchModelItem(budgetListData.budgetListId,
                            budgetListData.budgetListAttributes.name, "Budget", currencySymbol))
                    }
                } else {
                    budgetQueries.add(SearchModelItem(budgetListData.budgetListId,
                        budgetListData.budgetListAttributes.name, "Budget", ""))
                }
            }
        }
        return budgetQueries
    }

    private suspend fun searchAccounts(query: String): List<SearchModelItem>{
        val accountQueries = arrayListOf<SearchModelItem>()
        try {
            val webservice = searchService.searchAccounts(query)
            val body = webservice.body()
            if (body != null && webservice.isSuccessful){
                body.forEach { searchModelItem ->
                    accountQueries.add(SearchModelItem(searchModelItem.id,
                        searchModelItem.name, searchModelItem.type, searchModelItem.currencySymbol))
                }
            } else {
                val accountList = accountDao.searchAccountByName("%$query%")
                accountList.forEach { accountListData ->
                    accountQueries.add(SearchModelItem(accountListData.accountId,
                        accountListData.accountAttributes.name,
                        accountListData.accountAttributes.type, accountListData.accountAttributes.currency_symbol))
                }
            }
        } catch (exception: Exception){
            val accountList = accountDao.searchAccountByName("%$query%")
            accountList.forEach { accountListData ->
                accountQueries.add(SearchModelItem(accountListData.accountId,
                    accountListData.accountAttributes.name,
                    accountListData.accountAttributes.type, accountListData.accountAttributes.currency_symbol))
            }
        }
        return accountQueries
    }

    private suspend fun searchCategory(query: String): List<SearchModelItem>{
        val categoryQueries = arrayListOf<SearchModelItem>()
        try {
            val webservice = searchService.searchCategories(query)
            val body = webservice.body()
            if (body != null && webservice.isSuccessful){
                body.forEach { searchModelItem ->
                    categoryQueries.add(SearchModelItem(searchModelItem.id,
                        searchModelItem.name, "Category", ""))
                }
            } else {
                val categoryList = categoryDataDao.searchCategory("%$query%")
                categoryList.forEach { categoryListData ->
                    categoryQueries.add(SearchModelItem(categoryListData.categoryId,
                        categoryListData.categoryAttributes.name, "Category", ""))
                }
            }
        } catch (exception: Exception){
            val categoryList = categoryDataDao.searchCategory("%$query%")
            categoryList.forEach { categoryListData ->
                categoryQueries.add(SearchModelItem(categoryListData.categoryId,
                    categoryListData.categoryAttributes.name, "Category", ""))
            }
        }
        return categoryQueries
    }

    private suspend fun searchPiggyBanks(query: String): List<SearchModelItem>{
        val piggyBankQueries = arrayListOf<SearchModelItem>()
        try {
            val webservice = searchService.searchPiggyBanks(query)
            val body = webservice.body()
            if (body != null && webservice.isSuccessful){
                body.forEach { searchModelItem ->
                    piggyBankQueries.add(SearchModelItem(searchModelItem.id,
                        searchModelItem.name, "Piggy Bank", searchModelItem.currencySymbol))
                }
            } else {
                val piggyBankList = piggyDao.searchPiggyByName("%$query%")
                piggyBankList.forEach { piggyBankListData ->
                    piggyBankQueries.add(SearchModelItem(piggyBankListData.piggyId,
                        piggyBankListData.piggyAttributes.name, "Piggy Bank", piggyBankListData.piggyAttributes.currency_symbol))
                }
            }
        } catch (exception: Exception){
            val piggyBankList = piggyDao.searchPiggyByName("%$query%")
            piggyBankList.forEach { piggyBankListData ->
                piggyBankQueries.add(SearchModelItem(piggyBankListData.piggyId,
                    piggyBankListData.piggyAttributes.name, "Piggy Bank", piggyBankListData.piggyAttributes.currency_symbol))
            }
        }
        return piggyBankQueries
    }

    private suspend fun searchTags(query: String): List<SearchModelItem>{
        val tagsQueries = arrayListOf<SearchModelItem>()
        try {
            val webservice = searchService.searchTags(query)
            val body = webservice.body()
            if (body != null && webservice.isSuccessful){
                body.forEach { searchModelItem ->
                    tagsQueries.add(SearchModelItem(searchModelItem.id,
                        searchModelItem.name, "Tags", ""))
                }
            } else {
                val tagsList = tagsDataDao.searchTags("%$query%")
                tagsList.forEach { tagListData ->
                    tagsQueries.add(SearchModelItem(tagListData.tagsId,
                        tagListData.tagsAttributes.description, "Tags", ""))
                }
            }
        } catch (exception: Exception){
            val tagsList = tagsDataDao.searchTags("%$query%")
            tagsList.forEach { tagListData ->
                tagsQueries.add(SearchModelItem(tagListData.tagsId,
                    tagListData.tagsAttributes.description, "Tags", ""))
            }
        }
        return tagsQueries
    }

    private suspend fun searchTransaction(query: String): List<SearchModelItem>{
        val transactionQueries = arrayListOf<SearchModelItem>()
        try {
            /* Transaction searching is an intensive lookup operation, we will search on the server
             * ONLY if query string is longer than 4 characters. Why 4 you ask?
             * Ha!
             */
            if(query.length > 4){
                val webservice = searchService.searchTransactions(query)
                val body = webservice.body()
                if (body != null && webservice.isSuccessful){
                    body.forEach { searchModelItem ->
                        val transactionWebservice =
                            transactionService.getTransactionById(searchModelItem.id)
                        val transactionBody = transactionWebservice.body()
                        if (transactionBody != null && webservice.isSuccessful){
                            transactionBody.data.forEach { data ->
                                data.transactionAttributes.transactions.forEach { transactions ->
                                    transactionQueries.add(SearchModelItem(transactions.transaction_journal_id,
                                        searchModelItem.name + " (" + transactions.date.toLocalDate() + ")", transactions.transactionType, transactions.currency_symbol))
                                }
                            }
                        }

                    }
                } else {
                    val transactionList = transactionDao.searchTransactionListByDescription("%$query%")
                    transactionList.forEach { transaction ->
                        transactionQueries.add(SearchModelItem(transaction.transaction_journal_id,
                            transaction.description + " (" + transaction.date.toLocalDate() + ")", transaction.transactionType, transaction.currency_symbol))
                    }
                }
            } else {
                val transactionList = transactionDao.searchTransactionListByDescription("%$query%")
                transactionList.forEach { transaction ->
                    transactionQueries.add(SearchModelItem(transaction.transaction_journal_id,
                        transaction.description + " (" + transaction.date.toLocalDate() + ")", transaction.transactionType, transaction.currency_symbol))
                }
            }
        } catch (exception: Exception){
            val transactionList = transactionDao.searchTransactionListByDescription("%$query%")
            transactionList.forEach { transaction ->
                transactionQueries.add(SearchModelItem(transaction.transaction_journal_id,
                    transaction.description + " (" + transaction.date.toLocalDate() + ")",
                    transaction.transactionType, transaction.currency_symbol))
            }
        }
        return transactionQueries
    }

    private suspend fun searchCurrencies(query: String): List<SearchModelItem>{
        val currencyQueries = arrayListOf<SearchModelItem>()
        try {
            val webservice = searchService.searchCurrencies(query)
            val body = webservice.body()
            if (body != null && webservice.isSuccessful){
                body.forEach { searchModelItem ->
                    // `currencySymbol` will always be `NULL` because Firefly III returns `symbol`
                    currencyQueries.add(SearchModelItem(searchModelItem.id,
                            searchModelItem.name, "Currency", searchModelItem.currencySymbol))
                }
            } else {
                val currencyList = currencyDao.searchCurrency("%$query%")
                currencyList.forEach { currency ->
                    currencyQueries.add(SearchModelItem(currency.currencyId,
                        currency.currencyAttributes.name , "Currency", currency.currencyAttributes.symbol))
                }
            }
        } catch (exception: Exception){
            val currencyList = currencyDao.searchCurrency("%$query%")
            currencyList.forEach { currency ->
                currencyQueries.add(SearchModelItem(currency.currencyId,
                    currency.currencyAttributes.name + " (" +
                            currency.currencyAttributes.symbol + ")", "Currency", currency.currencyAttributes.symbol))
            }
        }
        return currencyQueries
    }

    private suspend fun searchBill(query: String): List<SearchModelItem>{
        val billQueries = arrayListOf<SearchModelItem>()
        try {
            val webservice = searchService.searchBills(query)
            val body = webservice.body()
            if (body != null && webservice.isSuccessful){
                body.forEach { searchModelItem ->
                    billQueries.add(SearchModelItem(searchModelItem.id,
                        searchModelItem.name, "Bills", ""))
                }
            } else {
                val billsList = billDao.searchBills("%$query%")
                billsList.forEach { bill ->
                    billQueries.add(SearchModelItem(bill.billId,
                        bill.billAttributes.name, "Bills", ""))
                }
            }
        } catch (exception: Exception){
            val billsList = billDao.searchBills("%$query%")
            billsList.forEach { bill ->
                billQueries.add(SearchModelItem(bill.billId,
                    bill.billAttributes.name, "Bills", ""))
            }
        }
        return billQueries
    }


}