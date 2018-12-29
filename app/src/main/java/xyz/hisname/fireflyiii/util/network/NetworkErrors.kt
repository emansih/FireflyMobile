package xyz.hisname.fireflyiii.util.network

class NetworkErrors(){

    companion object {
        fun getThrowableMessage(message: String): String{
            return if(message.startsWith("Certificate Pinning failure")){
                "Certificate pinning failure!"
            } else {
                message
            }
        }
    }
}