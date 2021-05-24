package com.arddev.absensi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.imaginativeworld.oopsnointernet.NoInternetDialog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private val FCR = 1
    private val FILECHOOSER_RESULTCODE = 1

    var mGeoLocationRequestOrigin: String? = null
    var mGeoLocationCallback: GeolocationPermissions.Callback? = null
    val MAX_PROGRESS = 100

    private var noInternetDialog: NoInternetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var webView = findViewById<WebView>(R.id.webview)

        /*
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }*/

        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.setGeolocationEnabled(true)
        webView.webViewClient = MyWebClient()


        //URL
        webView.loadUrl("https://serversatu.net/")




        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(this@MainActivity.packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        Log.e("Webview", "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath()
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile)
                        )
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent>
                intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOf<Intent>()
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)
                return true
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        AlertDialog.Builder(this@MainActivity)
                            .setMessage("Please turn ON the GPS to make app work smoothly")
                            .setNeutralButton(
                                android.R.string.ok,
                                DialogInterface.OnClickListener { _, _ ->
                                    mGeoLocationCallback = callback
                                    mGeoLocationRequestOrigin = origin
                                    ActivityCompat.requestPermissions(
                                        this@MainActivity,
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001
                                    )

                                })
                            .show()

                    } else {
                        //no explanation need we can request the locatio
                        mGeoLocationCallback = callback
                        mGeoLocationRequestOrigin = origin
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001
                        )
                    }
                } else {
                    //tell the webview that permission has granted
                    callback!!.invoke(origin, true, true)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1001 -> {
                //if permission is cancel result array would be empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    if (mGeoLocationCallback != null) {
                        mGeoLocationCallback!!.invoke(mGeoLocationRequestOrigin, true, true)
                    }
                } else {
                    //permission denied
                    if (mGeoLocationCallback != null) {
                        mGeoLocationCallback!!.invoke(mGeoLocationRequestOrigin, false, false)
                    }
                }
            }

        }
    }

    // Create an image file
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
        this.openFileChooser(uploadMsg, "*/*")
    }

    fun openFileChooser(
        uploadMsg: ValueCallback<Uri?>?,
        acceptType: String?
    ) {
        this.openFileChooser(uploadMsg, acceptType, null)
    }

    fun openFileChooser(
        uploadMsg: ValueCallback<Uri?>?,
        acceptType: String?,
        capture: String?
    ) {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        this@MainActivity.startActivityForResult(
            Intent.createChooser(i, "File Browser"),
            FILECHOOSER_RESULTCODE
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) { //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUMA!!.onReceiveValue(results)
            mUMA = null
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return
                val result =
                    if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
                mUM!!.onReceiveValue(result)
                mUM = null
            }
        }
    }

    override fun onBackPressed() {
        if (webview.canGoBack())
            webview.goBack()
        else
            super.onBackPressed()
    }

    inner class MyWebClient : WebViewClient(){

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            url: String?
        ): Boolean {
            webview.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            view?.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            webview.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            try {
                webview.stopLoading()
                //moveToSplash()
            } catch (e: Exception) {
            }

            view?.loadUrl("file:///android_asset/ErrorPage.html")
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler?.proceed()
        }


    }

    private fun moveToSplash() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    /*
    override fun onResume() {
        super.onResume()
        noInternetDialog = NoInternetDialog.Builder(this)
            .apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                        Log.e("Webview", "Connection : $hasActiveConnection")
                        if (hasActiveConnection){
                            progressBar.visibility == View.GONE
                        }else{
                            progressBar.visibility == View.VISIBLE
                        }
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
    }*/

}