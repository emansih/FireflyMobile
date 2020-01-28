package xyz.hisname.fireflyiii.fireflydao

import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

class DataFactory {

    companion object Factory {

        fun randomInt(): Int {
            return ThreadLocalRandom.current().nextInt(0, 1000 + 1)
        }

        fun randomLong(): Long {
            return randomInt().toLong()
        }

        fun randomBoolean(): Boolean {
            return Math.random() < 0.5
        }

        fun randomAccountType(): String {
            val accountType = arrayListOf("asset", "expense", "revenue")
            val elementNumber = Random.nextInt(accountType.size)
            return accountType[elementNumber]
        }

    }

}