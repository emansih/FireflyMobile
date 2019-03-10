package xyz.hisname.fireflyiii.util.network

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class GithubClientCert {

    companion object {
        fun getGithubSSLClient(): OkHttpClient{
            val certPinner = CertificatePinner.Builder()
                    .add("raw.githubusercontent.com",
                            "sha256/sm6xYAA3V3PtiyWIX6G/FY2kgHCRzR1k9XndcF5A0mg=")
                    .build()
            val okHttpClient = OkHttpClient.Builder()
                    .certificatePinner(certPinner)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build()
            return okHttpClient

        }
    }
}