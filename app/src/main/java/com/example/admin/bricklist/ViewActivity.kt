package com.example.admin.bricklist

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_view_row.*
import kotlinx.android.synthetic.main.activity_view_row.view.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ViewActivity : AppCompatActivity() {

    private var parentLinearLayout: LinearLayout? = null
    private val REQUEST_CODE = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        parentLinearLayout = findViewById<View>(R.id.parent_linear_layout) as LinearLayout
        displayInventoryParts()
    }

    override fun onBackPressed() {
        showMainActivity()
    }

    fun onPlus(v: View){
        val button : Button = v as Button
        val linearLayout : LinearLayout = button.parent.parent as LinearLayout

        if( linearLayout.quantityInStore.text.toString().toInt() == ( linearLayout.quantityInSet.text.toString().toInt() - 1) ){
            linearLayout.setBackgroundColor(0xFF00FF00.toInt())
            linearLayout.quantityInStore.text = (linearLayout.quantityInStore.text.toString().toInt() + 1).toString()
            MainActivity.dbHandler?.updateInventoryPartQuantityInSet( linearLayout.id, linearLayout.quantityInStore.text.toString())
            MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore = MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore!! + 1
        }
        else if( linearLayout.quantityInStore.text.toString().toInt() < linearLayout.quantityInSet.text.toString().toInt()){
            linearLayout.quantityInStore.text = (linearLayout.quantityInStore.text.toString().toInt() + 1).toString()
            MainActivity.dbHandler?.updateInventoryPartQuantityInSet( linearLayout.id, linearLayout.quantityInStore.text.toString())
            MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore = MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore!! + 1
        }

    }

    fun onMinus(v: View){
        val button : Button = v as Button
        val linearLayout : LinearLayout = button.parent.parent as LinearLayout

        if( linearLayout.quantityInStore.text == linearLayout.quantityInSet.text ){
            linearLayout.setBackgroundColor(0xFFFFFFFF.toInt())

            linearLayout.quantityInStore.text = (linearLayout.quantityInStore.text.toString().toInt() - 1 ).toString()
            MainActivity.dbHandler?.updateInventoryPartQuantityInSet( linearLayout.id, linearLayout.quantityInStore.text.toString() )
            MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore = MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore!! - 1
        }
        else if( linearLayout.quantityInStore.text.toString().toInt() > 0){
            linearLayout.quantityInStore.text = (linearLayout.quantityInStore.text.toString().toInt() - 1).toString()
            MainActivity.dbHandler?.updateInventoryPartQuantityInSet( linearLayout.id, linearLayout.quantityInStore.text.toString())
            MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore = MainActivity.dbHandler?.viewInventoryPartsList?.get(linearLayout.id - 1)?.QuantityInStore!! - 1
        }
    }

    fun onGenerateXML(v: View){

        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()

        val rootElement: Element = doc.createElement("INVENTORY")

        for( index in MainActivity.dbHandler?.viewInventoryPartsList?.indices!!){

            val qtyInSet = MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.QuantityInSet!!
            val qtyInStore = MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.QuantityInStore!!
            if( qtyInSet - qtyInStore != 0){

                val item: Element = doc.createElement("ITEM")

                val itemType: Element = doc.createElement("ITEMTYPE")
                itemType.appendChild(doc.createTextNode(MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.TypeID.toString()))

                val itemID: Element = doc.createElement("ITEMID")
                itemID.appendChild(doc.createTextNode(MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.ItemID.toString()))

                val color: Element = doc.createElement("COLOR")
                color.appendChild(doc.createTextNode(MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.ColorID.toString()))

                val qtyFilled: Element = doc.createElement("QTYFILLED")
                qtyFilled.appendChild(doc.createTextNode((qtyInSet - qtyInStore).toString()))

                item.appendChild(itemType)
                item.appendChild(itemID)
                item.appendChild(color)
                item.appendChild(qtyFilled)
                rootElement.appendChild(item)
            }
        }
        doc.appendChild(rootElement)

        val transformer : Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val path = Environment.getExternalStorageDirectory().toString()
        val outDir = File(path, "BricksToBuy")
        outDir.mkdir()
        val file = File(outDir, "bricksToBuy.xml")

        val result = StreamResult(file)
        val source = DOMSource(doc)
        transformer.transform(source, result)

        val toast = Toast.makeText(applicationContext, "XML generated", Toast.LENGTH_SHORT)
        toast.show()
        showMainActivity()
    }

    fun showMainActivity(){
        val i = Intent( this, MainActivity::class.java)
        startActivityForResult(i, REQUEST_CODE)
        finish()
    }

    private fun displayInventoryParts(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if(MainActivity.dbHandler?.viewInventoryPartsList!!.count() > 0){
            for(index in MainActivity.dbHandler?.viewInventoryPartsList?.indices!!){

                val itemID = MainActivity.dbHandler?.viewInventoryPartsList?.get(index)!!.ItemID
                val colorID = MainActivity.dbHandler?.viewInventoryPartsList?.get(index)!!.ColorID
                val bMap : Bitmap
                val rowView = inflater.inflate(R.layout.activity_view_row, null)

                if(MainActivity.dbHandler?.getImageOfBrick(itemID, colorID) != null)
                {
                    bMap = MainActivity.dbHandler?.getImageOfBrick(itemID, colorID)!!
                }
                else{
                    val notResizedBitMap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
                    bMap = Bitmap.createScaledBitmap(notResizedBitMap, 140, 140, false)
                }

                val imageView : ImageView = rowView.findViewById<View>(R.id.imgView) as ImageView
                imageView.setImageBitmap(bMap)

                val quantityInStoreTextView : TextView = rowView.findViewById<View>(R.id.quantityInStore) as TextView
                quantityInStoreTextView.text = MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.QuantityInStore.toString()

                val quantityInSetTextView : TextView = rowView.findViewById<View>(R.id.quantityInSet) as TextView
                quantityInSetTextView.text = MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.QuantityInSet.toString()

                val nameTextView : TextView = rowView.findViewById<View>(R.id.name) as TextView
                nameTextView.text = MainActivity.dbHandler?.getNameOfBrick(itemID, colorID)

                rowView.id = MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.id!!
                if( MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.QuantityInSet == MainActivity.dbHandler?.viewInventoryPartsList?.get(index)?.QuantityInStore){
                    rowView.setBackgroundColor(0xFF00FF00.toInt())
                }
                parentLinearLayout?.addView(rowView, parentLinearLayout?.childCount!!.minus(1))
            }
        }
        else{
            val toast = Toast.makeText(applicationContext, "Lack of bricks in project", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}
