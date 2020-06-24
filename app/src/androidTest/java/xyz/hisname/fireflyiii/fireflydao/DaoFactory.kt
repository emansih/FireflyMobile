package xyz.hisname.fireflyiii.fireflydao

import xyz.hisname.fireflyiii.fireflydao.DataFactory.Factory.randomAccountType
import xyz.hisname.fireflyiii.fireflydao.DataFactory.Factory.randomBoolean
import xyz.hisname.fireflyiii.fireflydao.DataFactory.Factory.randomInt
import xyz.hisname.fireflyiii.fireflydao.DataFactory.Factory.randomLong
import xyz.hisname.fireflyiii.repository.models.accounts.AccountAttributes
import xyz.hisname.fireflyiii.repository.models.accounts.AccountData
import xyz.hisname.fireflyiii.repository.models.category.CategoryAttributes
import xyz.hisname.fireflyiii.repository.models.category.CategoryData
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyAttributes
import xyz.hisname.fireflyiii.repository.models.currency.CurrencyData
import xyz.hisname.fireflyiii.repository.models.tags.TagsAttributes
import xyz.hisname.fireflyiii.repository.models.tags.TagsData

class DaoFactory {

    companion object Factory{

        fun makeAccount(): AccountData {
            return AccountData(
                    "", randomLong(),
                    AccountAttributes(
                            "", "", "", randomBoolean(), randomAccountType(), "" ,
                            randomInt(), "", 0.0, "", "",
                            "", "", "", "", "",
                            "", 0.0, "" ,"", "", "",
                            "","", "", randomBoolean()
                    )
            )
        }

        fun makeMultipleFakeAccount(numberOfAccount: Int): List<AccountData>{
            val accounts = mutableListOf<AccountData>()
            repeat(numberOfAccount){
                accounts.add(makeAccount())
            }
            return accounts
        }

        fun makeCounterfeitCurrency(): CurrencyData {
            return CurrencyData(
                    randomLong(),
                    CurrencyAttributes(
                            "", "", randomBoolean(), "", "", "",
                            randomInt(), randomBoolean()
                    ),
                    ""
            )
        }

        fun makeMultipleCounterfeitCurrency(numberOfCurrencies: Int): List<CurrencyData>{
            val currencies = mutableListOf<CurrencyData>()
            repeat(numberOfCurrencies){
                currencies.add(makeCounterfeitCurrency())
            }
            return currencies
        }

        fun makeCategory(): CategoryData {
            return CategoryData(
                    randomLong(),
                    CategoryAttributes(
                          "", "", ""
                    ),
                    ""
            )
        }

        fun makeMultipleCategory(numberOfCategory: Int): List<CategoryData>{
            val category = mutableListOf<CategoryData>()
            repeat(numberOfCategory){
                category.add(makeCategory())
            }
            return category
        }

        fun makeTags(): TagsData {
            return TagsData(
                    TagsAttributes(
                            "", "", "", "",
                            "", "", "", ""
                    ),
                    randomLong(),
                    ""
            )
        }

        fun makeMultipleTags(numberOfTags: Int): List<TagsData>{
            val tags = mutableListOf<TagsData>()
            repeat(numberOfTags){
                tags.add(makeTags())
            }
            return tags
        }
    }

}