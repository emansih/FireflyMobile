package xyz.hisname.fireflyiii.util.network

class NetworkErrors(){

    companion object {
        fun getThrowableMessage(message: String): String{
            return if(message.startsWith("Certificate Pinning failure")){
                "Certificate pinning failure!"
            } else if(message.startsWith("CLEARTEXT communication to")){
                "Please use HTTPS in your URL"
            } else {
                message
            }
        }
    }
}