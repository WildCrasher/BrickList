package com.example.admin.bricklist

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.example.admin.bricklist.MyDBHandler.Companion.APP_SETTINGS_ARCHIVE
import com.example.admin.bricklist.MyDBHandler.Companion.APP_SETTINGS_URL_PREFIX
import com.example.admin.bricklist.MyDBHandler.Companion.DEFAULT_ARCHIVE
import com.example.admin.bricklist.MyDBHandler.Companion.DEFAULT_URL_PREFIX
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_app_settings.*

class AppSettingsActivity : AppCompatActivity() {

    private val REQUEST_CODE = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        urlPrefix.setText( preferences.getString(APP_SETTINGS_URL_PREFIX, DEFAULT_URL_PREFIX) )
        toggleButton.isChecked = preferences.getString(APP_SETTINGS_ARCHIVE, DEFAULT_ARCHIVE) == "NO"
    }

    override fun onBackPressed() {
        showMainActivity()
    }

    fun onCommitChanges(v : View){
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        if(toggleButton.isChecked){
            editor.putString(APP_SETTINGS_ARCHIVE, "NO")
        }
        else{
            editor.putString(APP_SETTINGS_ARCHIVE, "YES")
        }

        editor.putString(APP_SETTINGS_URL_PREFIX, urlPrefix.text.toString())
        editor.apply()
        showMainActivity()
    }

    fun showMainActivity(){
        val i = Intent( this, MainActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
        finish()
    }
}
