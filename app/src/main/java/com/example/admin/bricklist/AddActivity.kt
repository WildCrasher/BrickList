package com.example.admin.bricklist

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import com.example.admin.bricklist.MainActivity.Companion.dbHandler
import kotlinx.android.synthetic.main.activity_add.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream

class AddActivity : AppCompatActivity() {

    private val REQUEST_CODE = 10000
    private var disable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

    }

    override fun onBackPressed() {
        if(!disable){
            showMainActivity()
        }
        else{
            val toast = Toast.makeText(applicationContext, "Waiting for downloading project.", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun showMainActivity(){
        val i = Intent( this, MainActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
        finish()
    }

    fun onAdd(v: View){
        disable = true
        commitChanges.isEnabled = false
        val task=BgTask()
        task.execute("Download")
    }

    private inner class BgTask: AsyncTask<String, Int, String>(){

        override fun onPreExecute(){
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?){
            super.onPostExecute(result)
            showMainActivity()
        }

        override fun doInBackground(vararg p0: String?): String{
            val preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
            val response = khttp.get(
                    url = preferences.getString(MyDBHandler.APP_SETTINGS_URL_PREFIX, MyDBHandler.DEFAULT_URL_PREFIX) + projectNumber.text.toString() + ".xml"
            )
            if( response.statusCode == 200){
                val XMLFile = ByteArrayInputStream(response.text.toByteArray())
                parserXML(XMLFile)
                return "Downloaded"
            }
            else{
                val toast = Toast.makeText(applicationContext, "No project on this address. Try to change URL prefix.", Toast.LENGTH_SHORT)
                toast.show()
                return "No resource"
            }
        }

    }

    private fun parserXML( input : InputStream){
        val parserFactory : XmlPullParserFactory? = XmlPullParserFactory.newInstance()
        val parser : XmlPullParser? = parserFactory?.newPullParser()
        parser?.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser?.setInput(input, null)
        processParser(parser!!)
    }

    private fun processParser(parser : XmlPullParser){
        dbHandler?.openDataBase()
        val EMPTY_STORE = 0
        val ind = dbHandler?.getLastInventoryID()!! + 1
        var eventType : Int = parser.eventType
        val inventoryPartsList: ArrayList<InventoryPart>? = ArrayList<InventoryPart>()
        val currentInventory : Inventory? = Inventory(ind, urlPrefixEditText.text.toString(), 1, 0)
        var currentInventoryPart : InventoryPart?
        var markName : String?
        var indexOfInventoryPartsList : Int = -1
        var lastInventoryPartIndex : Int = dbHandler?.getLastInventoryPartID()!!

        while( eventType != XmlPullParser.END_DOCUMENT ){

            if( eventType == XmlPullParser.START_TAG){
                markName = parser.name
                if( markName == "ITEM")
                {
                    currentInventoryPart = InventoryPart()
                    currentInventoryPart.id = lastInventoryPartIndex + 1
                    lastInventoryPartIndex++
                    currentInventoryPart.InventoryID = ind
                    currentInventoryPart.QuantityInStore = EMPTY_STORE
                    inventoryPartsList?.add(currentInventoryPart)
                    indexOfInventoryPartsList++
                }
                else if( markName == "ITEMID")
                {
                    inventoryPartsList?.get(indexOfInventoryPartsList)?.ItemID = dbHandler?.getItemID(parser.nextText())!!
                }
                else if( markName == "QTY")
                {
                    inventoryPartsList?.get(indexOfInventoryPartsList)?.QuantityInSet = parser.nextText().toInt()
                }
                else if( markName == "COLOR")
                {
                    inventoryPartsList?.get(indexOfInventoryPartsList)?.ColorID = parser.nextText().toInt()
                }
                else if( markName == "ITEMTYPE")
                {
                    inventoryPartsList?.get(indexOfInventoryPartsList)?.TypeID = dbHandler?.getTypeID(parser.nextText())!!
                }
                else if( markName == "EXTRA")
                {
                    val extra : String = parser.nextText()
                    if( extra != "N" ) {
                    }
                }
            }

            eventType = parser.next()
        }

        dbHandler?.addInventoryToDB(currentInventory!!)
        dbHandler?.inventoryList?.add(currentInventory!!)
        for( inventoryPart in inventoryPartsList!!){
            dbHandler?.addInventoryPartToDB(inventoryPart)
            MainActivity.dbHandler?.downloadAndAddToDBImageOfBrick(inventoryPart.ItemID, inventoryPart.ColorID)
        }

        dbHandler?.closeDatabase()

    }

    fun provideImage( itemID : Int, colorID : Int){




    }

}
