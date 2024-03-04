package ru.pakarpichev.cryptographytest

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.Certificate
import java.util.Calendar
import java.util.GregorianCalendar
import javax.security.auth.x500.X500Principal

class KeyStoreInit {

    var isPrivateKeyExist = mutableStateOf(true)
        private set

    init {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val privateKey = keyStore.getKey("testKey", null) as PrivateKey?
        if (privateKey == null){
            isPrivateKeyExist.value = false
        }
    }

    fun generateKey() {
        // Setting the key expiration date
        val startDate = GregorianCalendar()
        val endDate = GregorianCalendar()
        endDate.add(Calendar.YEAR, 1)

        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore"
        )

        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            "testKey",
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).run {
            setCertificateSerialNumber(BigInteger.valueOf(777))
            setCertificateSubject(X500Principal("CN=testKey"))
            setDigests(KeyProperties.DIGEST_SHA256)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            setCertificateNotBefore(startDate.time)
            setCertificateNotAfter(endDate.time)
            build()
        }
        //Generate pair of keys
        keyPairGenerator.initialize(parameterSpec)
        val keyPair = keyPairGenerator.generateKeyPair()

    }

    fun signData(file: ByteArray): String {

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val privateKey = keyStore.getKey("testKey", null) as PrivateKey?

        val signature: ByteArray? = Signature.getInstance("SHA256withRSA").run {
            initSign(privateKey)
            update(file)
            sign()
        }

        return Base64.encodeToString(signature, Base64.DEFAULT)
    }

    fun verifyData(signatureResult: String, file: ByteArray): Boolean {
        //Get the Keystore instance
        var result = false
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        //Get the certificate from the keystore
        val certificate: Certificate? = keyStore.getCertificate("testKey")
        Log.d("verifyDataFunc", "cert = $certificate")

        if (certificate != null) {
            //Decode the signature value
            val signature: ByteArray = Base64.decode(signatureResult, Base64.DEFAULT)

            //Check if the signature is valid. We use RSA algorithm along SHA-256 digest algorithm
            val isValid: Boolean = Signature.getInstance("SHA256withRSA").run {
                initVerify(certificate)
                update(file)
                verify(signature)
            }
            result = isValid
        }
        return result
    }

}