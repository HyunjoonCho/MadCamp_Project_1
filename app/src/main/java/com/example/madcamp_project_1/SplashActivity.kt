package com.example.madcamp_project_1

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat

//<div>Icons made by <a href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>

class SplashActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startAct()
            }
            else {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE),
                    100
                )
                //callback onRequestPermissionsResult
            }
        } else {
            startAct()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startAct()
            } else {
                Handler().postDelayed({
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
                    finish()
                }, 800)
            }
        }
    }

    fun startAct(){
        // This method will be executed once the timer is over
        // Start your app main activity
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))

            // close this activity
            finish()
        }, 800)
    }
}