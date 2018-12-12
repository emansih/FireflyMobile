package xyz.hisname.fireflyiii.util.network

class NetworkErrors(){

    companion object {
        fun getThrowableMessage(message: String): String{
            if(message.startsWith("Certificate Pinning failure")){
                return "Certificate pinning failure!"
            } else {
                return "Exception occurred"
            }
        }
    }
}