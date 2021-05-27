package com.arddev.absensi

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arddev.absensi.utils.CustomDialog
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText
    private lateinit var mToken: EditText
    private lateinit var mButton: CardView
    private lateinit var mUsernameError: TextView
    private lateinit var mPasswordError: TextView
    private lateinit var mTokenError: TextView
    private lateinit var mLoginError: TextView
    private var requestQueue: RequestQueue? = null
    private var isLoading: Boolean = false

    private val sheetURL = "https://docs.google.com/spreadsheets/d/101L-l-n_xQ5DBjoKA-OLJsTDXr_0_B0OKRG6J5i-i74/edit#gid=0"
    private val sheetName = "UserData"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mUsername = findViewById(R.id.username_input)
        mPassword = findViewById(R.id.password_input)
        mToken = findViewById(R.id.token_input)
        mButton = findViewById(R.id.loginButton)

        mUsernameError = findViewById(R.id.usernameError)
        mPasswordError = findViewById(R.id.passwordError)
        mTokenError = findViewById(R.id.tokenError)
        mLoginError = findViewById(R.id.loginError)

        requestQueue = Volley.newRequestQueue(this)

        mButton.setOnClickListener {
            if (!isLoading){
                checkInput()
            }else null
        }

    }

    private fun checkInput() {
        isLoading = true
        mLoginError.visibility = View.GONE
        mButton.setCardBackgroundColor(Color.parseColor("#d2d2d2"))


        val usernameData = isInputEmpty(mUsername.text.toString())
        val passwordData = isInputEmpty(mPassword.text.toString())
        val tokenData = isInputEmpty(mToken.text.toString())

        if (!usernameData && !passwordData && !tokenData){
            Log.d("Button Test", "Input Valid")
            //saveData()
            //checkToken()
            checkUser()
            mUsernameError.visibility = View.GONE
            mPasswordError.visibility = View.GONE
            mTokenError.visibility = View.GONE
        }else{
            isLoading = false

            if (usernameData){
                mUsernameError.visibility = View.VISIBLE
            }else{
                mUsernameError.visibility = View.GONE
            }

            if (passwordData){
                mPasswordError.visibility = View.VISIBLE
            }else{
                mPasswordError.visibility = View.GONE
            }

            if (tokenData){
                mTokenError.visibility = View.VISIBLE
            }else{
                mTokenError.visibility = View.GONE
            }
        }

    }

    private fun checkUser(){
        val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val userName = mUsername?.text
        val userPassword = mPassword?.text
        val userToken = mToken?.text

        val url = "https://script.google.com/macros/s/AKfycbxz5L_AIc8sHhOHg61nLuA6k6ySdqiO82hPoqvLLrWkBJXaZwRX8fVnfxiyazLM7aGC/exec?action=getUserData&username=$userName&password=$userPassword&token=$userToken&sheetURL=$sheetURL&sheetName=$sheetName"

        val request = JsonObjectRequest(Request.Method.POST, url, null, Response.Listener {
                response ->try {
            Log.d("LOGIN ACTIVITY", "RESULT : $response")
            var strResp = response.toString()
            val jsonObj: JSONObject = JSONObject(strResp)

            val data = jsonObj.getString("status")
            val message = jsonObj.getString("message")
            val cek = data.equals("400")
            Log.d("LOGIN ACTIVITY", "DATA : $cek")


            if (!cek){

                Log.d("LOGIN ACTIVITY", "User found!")
                var userName = ""
                var userPassword = ""
                var userToken = ""

                val jsonArray = jsonObj.optJSONArray("result")

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    userName = jsonObject.optString("userName")
                    userPassword = jsonObject.optString("userPassword")
                    userToken = jsonObject.optString("userToken")
                }

                saveData(userName, userPassword, userToken)

            }else{
                isLoading = false
                mButton.setCardBackgroundColor(Color.parseColor("#FF7849"))
                mLoginError.text = message
                mLoginError.visibility = View.VISIBLE
                Log.d("LOGIN ACTIVITY", "User not found!")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        }, Response.ErrorListener { error -> error.printStackTrace() })
        requestQueue?.add(request)
    }

    private fun doLogin(
        userName: String,
        userPassword: String,
        userToken: String
    ) {
        isLoading = false
        mButton.setCardBackgroundColor(Color.parseColor("#FF7849"))
        val generatedUrl = "?user=$userName&pass=$userPassword&token=$userToken"

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("parameter", generatedUrl)
        startActivity(intent)
        finish()
    }

    private fun saveData(
        userName: String,
        userPassword: String,
        userToken: String
    ) {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        // write all the data entered by the user in SharedPreference and apply

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putString("userName", userName)
        myEdit.putString("userPassword", userPassword)
        myEdit.putString("userToken", userToken)
        myEdit.apply()

        doLogin(userName, userPassword, userToken)

        Log.d("LOGIN ACTIVITY", "Data saved")
    }

    private fun isInputEmpty(input: String): Boolean{
        return input.isEmpty()
    }
}