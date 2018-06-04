package com.example.admin.bricklist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
//import kotlinx.android.synthetic.main.activity_main.*
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import android.preference.PreferenceManager
import com.example.admin.bricklist.MyDBHandler.Companion.APP_SETTINGS_ARCHIVE
import com.example.admin.bricklist.MyDBHandler.Companion.APP_SETTINGS_URL_PREFIX
import com.example.admin.bricklist.MyDBHandler.Companion.DEFAULT_ARCHIVE
import com.example.admin.bricklist.MyDBHandler.Companion.DEFAULT_URL_PREFIX


class MainActivity : AppCompatActivity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var dbHandler : MyDBHandler? = null
    }

    private var parentLinearLayout: LinearLayout? = null
    private val REQUEST_CODE = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHandler = MyDBHandler(this)
        dbHandler?.openDataBase()
        parentLinearLayout = findViewById<View>(R.id.parent_linear_layout) as LinearLayout

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        if(!preferences.contains(APP_SETTINGS_URL_PREFIX)){
            editor.putString(APP_SETTINGS_URL_PREFIX, DEFAULT_URL_PREFIX)
            editor.apply()
        }
        if(!preferences.contains(APP_SETTINGS_ARCHIVE)){
            editor.putString(APP_SETTINGS_ARCHIVE, DEFAULT_ARCHIVE)
            editor.apply()
        }

        displayInventories()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInf = menuInflater
        menuInf.inflate(R.menu.menu_group, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.urlPrefix -> {
                showSettingsActivity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayInventories() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val isArchive = preferences.getString(APP_SETTINGS_ARCHIVE, DEFAULT_ARCHIVE)

        if(dbHandler?.inventoryList!!.count() > 0){
            for(index in dbHandler?.inventoryList?.indices!!){
                if(dbHandler?.inventoryList?.get(index)?.Active != 0 || isArchive == "NO"){
                    val rowView = inflater.inflate(R.layout.activity_row, null)
                    val textView : TextView = rowView.findViewById<View>(R.id.projectName) as TextView
                    textView.text = dbHandler?.inventoryList?.get(index)?.Name
                    rowView.id = dbHandler?.inventoryList?.get(index)?.id!!
                    parentLinearLayout?.addView(rowView, parentLinearLayout?.childCount!!.minus(1))
                }
            }
        }
        else{
            val toast = Toast.makeText(applicationContext, "Lack of projects", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun onView(v: View){
        val button : Button = v as Button
        val linearLayout : LinearLayout = button.parent as LinearLayout
        val index: Int = linearLayout.id

        dbHandler?.readInventoryParts(index)
        showViewActivity()
    }

    fun onArchive(v: View){
        val button : Button = v as Button
        val linearLayout : LinearLayout = button.parent as LinearLayout
        val index: Int = linearLayout.id
        dbHandler?.updateInventoryActive(index)
        hideInventory(linearLayout)
    }

    fun onAddProject(v: View){
        showAddActivity()
    }

    private fun showAddActivity(){
        val i = Intent( this, AddActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
        finish()
    }

    private fun showViewActivity(){
        val i = Intent( this, ViewActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
    }

    private fun showSettingsActivity(){
        val i = Intent( this, AppSettingsActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
        finish()
    }

    fun hideInventory(linearLayout: LinearLayout ) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if( preferences.getString(APP_SETTINGS_ARCHIVE, DEFAULT_ARCHIVE) == "YES" ){
            linearLayout.visibility = View.GONE
        }
    }
}
