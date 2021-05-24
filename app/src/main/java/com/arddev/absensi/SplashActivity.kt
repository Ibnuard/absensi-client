package com.arddev.absensi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.imaginativeworld.oopsnointernet.ConnectionCallback
import org.imaginativeworld.oopsnointernet.NoInternetDialog
import org.imaginativeworld.oopsnointernet.NoInternetSnackbar


class SplashActivity : AppCompatActivity() {
    // No Internet Dialog
    private var noInternetDialog: NoInternetDialog? = null
    // No Internet Snackbar
    private var noInternetSnackbar: NoInternetSnackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //hide statusbar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN


        //loading

        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
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