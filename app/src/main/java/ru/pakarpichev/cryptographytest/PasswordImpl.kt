package ru.pakarpichev.cryptographytest

import android.content.Context
import androidx.activity.ComponentActivity

class PasswordImpl(context: Context, userPassword: String) {

    val isPasswordExist = checkIsPasswordExist(context)

    val isPasswordCorrect = checkPassword(context, userPassword)

    fun createPassword(context: Context, userPassword: String) {
        val sharedPrefs =
            context.getSharedPreferences("passSharedPreferences", ComponentActivity.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.apply {
            this.putString("password", userPassword)
            apply()
        }
    }
    // Check if password that input by user correct or not
    fun checkPassword(context: Context, userPassword: String): Boolean {
        var result = false
        val sharedPrefs = context.getSharedPreferences(
            "passSharedPreferences",
            ComponentActivity.MODE_PRIVATE
        )
        val getSavedPassword = sharedPrefs.getString("password", null)
        getSavedPassword?.let {
            result = it == userPassword
        }
        return result
    }
    //Check if password even created or not
    private fun checkIsPasswordExist(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(
            "passSharedPreferences",
            ComponentActivity.MODE_PRIVATE
        )
        return (sharedPrefs.getString("password", null) != null)
    }
}