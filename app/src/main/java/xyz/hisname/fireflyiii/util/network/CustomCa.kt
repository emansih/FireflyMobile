package xyz.hisname.fireflyiii.util.network

import okio.Buffer
import xyz.hisname.fireflyiii.util.FileUtils
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.*


internal class CustomCa(private val customCert: File) {

    private val trustManager by lazy { customTrustManager(customCaFileToString()) }

    fun getCustomSSL(): SSLSocketFactory{
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        return sslContext.socketFactory
    }

    fun getCustomTrust(): X509TrustManager{
        return trustManager
    }

    private fun customCaFileToString(): InputStream{
        val cert = FileUtils.readFileContent(customCert)
        return Buffer().writeUtf8(cert).inputStream()
    }

    private fun customTrustManager(input: InputStream): X509TrustManager{
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificates = certificateFactory.generateCertificates(input)
        // Super secure password
        val password = "hunter2".toCharArray()
        val keyStore: KeyStore = newEmptyKeyStore(password)
        for ((index, certificate) in certificates.withIndex()) {
            val certificateAlias = index.toString()
            keyStore.setCertificateEntry(certificateAlias, certificate)
        }
        val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, password)
        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        return trustManagers[0] as X509TrustManager
    }

    private fun newEmptyKeyStore(password: CharArray): KeyStore{
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        val input: InputStream? = null
        keyStore.load(input, password)
        return keyStore
    }
}