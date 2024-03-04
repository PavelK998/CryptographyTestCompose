package ru.pakarpichev.cryptographytest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.pakarpichev.cryptographytest.ui.theme.CryptographyTestTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptographyTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var passwordInput by remember {
                        mutableStateOf("")
                    }

                    var isPasswordDialogOpen by remember {
                        mutableStateOf(true)
                    }
                    var isKeyExistDialogOpen by remember {
                        mutableStateOf(false)
                    }
                    var byteArray by remember() {
                        mutableStateOf<ByteArray?>(null)
                    }
                    var signatureResult by remember() {
                        mutableStateOf("")
                    }
                    var userChoose by remember {
                        mutableStateOf("")
                    }
                    var dataVerifyResult by remember {
                        mutableStateOf<Boolean?>(null)
                    }
                    val keyStore = KeyStoreInit()
                    val passwordCheck = PasswordImpl(this, passwordInput)

                    if (isPasswordDialogOpen) {
                        AlertDialog(
                            onDismissRequest = {
                                Toast.makeText(
                                    this,
                                    "Please enter password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            title = {
                                if (passwordCheck.isPasswordExist) {
                                    Text(text = "Enter the password:")
                                } else {
                                    Text(text = "Create password:")
                                }
                            },
                            text = {
                                TextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it })
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (passwordInput.isNotBlank()) {
                                        if (passwordCheck.isPasswordExist) {
                                            if (passwordCheck.isPasswordCorrect) {
                                                isPasswordDialogOpen = false
                                            } else{
                                                Toast.makeText(
                                                    this,
                                                    "Wrong password",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            passwordCheck.createPassword(this, passwordInput)
                                            isPasswordDialogOpen = false
                                        }

                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Password can't be empty",
                                            Toast.LENGTH_SHORT).show()
                                    }

                                }) {
                                    Text(text = "Ok")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { this.finish() }) {
                                    Text(text = "Dismiss")
                                }
                            }
                        )
                    }
                    else {
                        if (!keyStore.isPrivateKeyExist.value) {
                            isKeyExistDialogOpen = true
                        }
                        if (isKeyExistDialogOpen){
                            AlertDialog(
                                onDismissRequest = {
                                    Toast.makeText(
                                        this,
                                        "Please choose something",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                text = {
                                    Text(text = "You don't have a private key on you device, " +
                                            "do you want to create it?")
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        keyStore.generateKey()
                                        isKeyExistDialogOpen = false
                                    }) {
                                        Text(text = "Ok")
                                    }
                                },
                                dismissButton = {
                                    Button(onClick = {
                                        Toast.makeText(
                                            this,
                                            "You can't sign data without private key, " +
                                                    "relaunch app to create it",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        this.finish()
                                    }) {
                                        Text(text = "Exit")
                                    }
                                }
                            )
                        }
                        val singlePhotoPicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent(),
                            onResult = { uri ->
                                uri?.let {
                                    userChoose = it.encodedPath.toString()
                                    contentResolver.openInputStream(it).use { stream ->
                                        byteArray = stream?.readBytes()
                                    }
                                }
                            }
                        )

                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Your choose:")
                            Text(text = userChoose)
                            Button(onClick = {
                                singlePhotoPicker.launch("image/*")
                            }) {
                                Text(text = "Pick photo")
                            }
                            Row (
                                Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ){
                                Button(onClick = {
                                    if (keyStore.isPrivateKeyExist.value){
                                        byteArray?.let {
                                            keyStore.signData(it).let { result ->
                                                signatureResult = result
                                            }
                                        }
                                    }

                                }) {
                                    Text(text = "Sign data")
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                                Button(onClick = {
                                    if (keyStore.isPrivateKeyExist.value) {
                                        byteArray?.let {
                                            dataVerifyResult = keyStore.verifyData(signatureResult, it)
                                        }
                                    }
                                }) {
                                    Text(text = "Verify")
                                }
                            }
                            Spacer(modifier = Modifier.height(15.dp))
                            if (dataVerifyResult != null) {
                                Text(text = "Is data verified successfully?")
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = dataVerifyResult.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}