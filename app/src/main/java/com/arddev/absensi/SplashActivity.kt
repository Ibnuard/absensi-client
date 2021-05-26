package com.arddev.absensi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.imaginativeworld.oopsnointernet.ConnectionCallback
import org.imaginativeworld.oopsnointernet.NoInternetDialog
import org.imaginativeworld.oopsnointernet.NoInternetSnackbar
import org.json.JSONException
import org.json.JSONObject


class SplashActivity : AppCompatActivity() {
    // No Internet Dialog
    private var noInternetDialog: NoInternetDialog? = null
    // No Internet Snackbar
    private var noInternetSnackbar: NoInternetSnackbar? = null
    private var requestQueue: RequestQueue? = null

    private val sheetURL = "https://docs.google.com/spreadsheets/d/101L-l-n_xQ5DBjoKA-OLJsTDXr_0_B0OKRG6J5i-i74/edit#gid=0"
    private val sheetName = "UserData"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //hide statusbar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val sh = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userName = sh.getString("userName", "")
        val userPassword = sh.getString("userPassword", "")
        val userToken = sh.getString("userToken", "")

        requestQueue = Volley.newRequestQueue(this)

        //loading

        Handler().postDelayed({
            if (userName!!.isNotEmpty() && userPassword!!.isNotEmpty() && userToken!!.isNotEmpty()){
                checkUser(userName, userPassword, userToken)
            }else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)
    }

    private fun checkUser(
        userName: String,
        userPassword: String,
        userToken: String
    ) {

        val url = "https://script.google.com/macros/s/AKfycbxz5L_AIc8sHhOHg61nLuA6k6ySdqiO82hPoqvLLrWkBJXaZwRX8fVnfxiyazLM7aGC/exec?action=getUserData&username=$userName&password=$userPassword&token=$userToken&sheetURL=$sheetURL&sheetName=$sheetName"

        val request = JsonObjectRequest(Request.Method.POST, url, null, Response.Listener {
                response ->try {
            Log.d("LOGIN ACTIVITY", "RESULT : $response")
            var strResp = response.toString()
            val jsonObj: JSONObject = JSONObject(strResp)

            val data = jsonObj.getString("status")
            val cek = data.equals("400")
            Log.d("LOGIN ACTIVITY", "DATA : $cek")


            if (!cek){
                val generatedUrl = "?user=$userName&pass=$userPassword&token=$userToken"

                doLogin(generatedUrl)

            }else{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                Log.d("LOGIN ACTIVITY", "User not found!")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        }, Response.ErrorListener { error -> error.printStackTrace() })
        requestQueue?.add(request)

    }

    private fun doLogin(generatedUrl: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("parameter", generatedUrl)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        noInternetDialog = NoInternetDialog.Builder(this)
            .apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }
                cancelable = false // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional

                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
            .build()
    }

    override fun onPause() {
        super.onPause()
        noInternetDialog?.destroy()
    }
}