package com.arddev.absensi

import android.content.Context
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
import org.json.JSONArray
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
    private var requestQueue: RequestQueue? = null

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

        requestQueue = Volley.newRequestQueue(this)

        mButton.setOnClickListener {
            checkInput()
        }

    }

    private fun checkInput() {
        val usernameData = isInputEmpty(mUsername.text.toString())
        val passwordData = isInputEmpty(mPassword.text.toString())
        val tokenData = isInputEmpty(mToken.text.toString())

        if (!usernameData && !passwordData && !tokenData){
            Log.d("Button Test", "Input Valid")
            //saveData()
            checkToken()
            mUsernameError.visibility = View.GONE
            mPasswordError.visibility = View.GONE
            mTokenError.visibility = View.GONE
        }else{
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

    private fun checkToken() {
        val url = "https://script.google.com/macros/s/AKfycbx1VbUK3cjphDYRkpsZmgoqmaV_3R842geCD7Ac8RyryFJ8w_XzCTp-bF_U_Lt1Z1D4/exec?action=checkToken&username=Ibnu"
        val request = JsonObjectRequest(Request.Method.POST, url, null, Response.Listener {
                response ->try {
            Log.d("LOGIN ACTIVITY", "RESULT : $response")
            var strResp = response.toString()
            val jsonObj: JSONObject = JSONObject(strResp)

            val token = jsonObj.getString("result")

            Log.d("LOGIN ACTIVITY", "TOKEN : $token")

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        }, Response.ErrorListener { error -> error.printStackTrace() })
        requestQueue?.add(request)
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        // write all the data entered by the user in SharedPreference and apply

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putString("username", mUsername.text.toString())
        myEdit.putString("password", mPassword.text.toString())
        myEdit.putString("token", mToken.text.toString())
        myEdit.putString("deviceToken", deviceID)
        myEdit.apply()
        Log.d("LOGIN ACTIVITY", "Data saved")
    }

    private fun isInputEmpty(input: String): Boolean{
        return input.isEmpty()
    }
}