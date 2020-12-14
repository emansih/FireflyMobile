package xyz.hisname.fireflyiii.repository.attachment

enum class AttachableType {

    TRANSACTION {
        override fun toString(): String {
            return "TransactionJournal"
        }
    },

    BILL {
        override fun toString(): String {
            return "Bill"
        }
    },

    PIGGYBANK{
        override fun toString(): String {
            return "PiggyBank"
        }
    }
}