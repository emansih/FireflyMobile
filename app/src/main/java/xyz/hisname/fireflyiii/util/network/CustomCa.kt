/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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