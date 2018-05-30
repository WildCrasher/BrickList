package com.example.admin.bricklist

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.nio.file.Files.exists
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.text.style.ImageSpan
import android.util.Base64
import java.io.FileOutputStream
import java.io.IOException


class MyDBHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1){
    companion object {
        val DATABASE_NAME = "BrickList.sqlite"
        var DATABASE_LOCATION = "/data/data/com.example.admin/databases/"
        val TABLE_INVENTORIES = "Inventories"
        val COLUMN_ID = "id"
        val COLUMN_NAME = "Name"
        val COLUMN_ACTIVE = "Active"
        val COLUMN_LAST_ACCESSED = "LastAccessed"
        val APP_SETTINGS_URL_PREFIX = "APP_SETTINGS_URL_PREFIX"
        val DEFAULT_URL_PREFIX = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
        val APP_SETTINGS_ARCHIVE = "APP_SETTINGS_ARCHIVE"
        val DEFAULT_ARCHIVE = "YES"
    }


    private var dataBase: SQLiteDatabase? = null

    var viewInventoryPartsList: ArrayList<InventoryPart>? = ArrayList<InventoryPart>()
    var inventoryList: ArrayList<Inventory>? = null

    override fun onCreate( db: SQLiteDatabase) {

    }

    override fun onUpgrade(p0: SQLiteDatabase, oldVersion: Int, newVerison: Int){
    }

    fun deleteInventory(index: Int){
        dataBase?.execSQL("DELETE FROM Inventories WHERE id="+index.toString())

    }

    fun openDataBase() {
        val dbPath = context.getDatabasePath(MyDBHandler.DATABASE_NAME).path

        DATABASE_LOCATION = "/data/data/" + context.getPackageName() + "/databases/"

        val dbExist = checkDataBase()
        if(dbExist){

        }
        else{
            this.writableDatabase
            copyDataBase()
        }
        dataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        inventoryList = readInventory()
    }

    private fun checkDataBase(): Boolean {

        val dbPath = context.getDatabasePath(MyDBHandler.DATABASE_NAME).path
        var checkDB: SQLiteDatabase? = null

        try {
           // val myPath = DATABASE_LOCATION + DATABASE_NAME
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

        } catch (e: SQLiteException) {

            //database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close()

        }

        return if (checkDB != null) true else false
    }

    fun closeDatabase() {
        if (dataBase != null) {
            dataBase!!.close()
        }
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val Input = context.assets.open(DATABASE_NAME)
        val outFileName = DATABASE_LOCATION + DATABASE_NAME
        val Output = FileOutputStream(outFileName)
        val Buffer = ByteArray(1024)
        var Length: Int
        Length = Input.read(Buffer)
        while (Length > 0) {
            Output.write(Buffer, 0, Length)
            Length = Input.read(Buffer)
        }
        Output.flush()
        Output.close()
        Input.close()
    }

    fun addInventoryToDB(inventory: Inventory){
        openDataBase()
        val values = ContentValues()
        values.put(COLUMN_ID, inventory.id)
        values.put(COLUMN_NAME, inventory.Name)
        values.put(COLUMN_ACTIVE, inventory.Active)
        values.put(COLUMN_LAST_ACCESSED, inventory.LastAccessed)
        dataBase?.insert(TABLE_INVENTORIES, null, values)
        closeDatabase()
    }

    fun addInventoryPartToDB(inventoryPart : InventoryPart){
        openDataBase()
        val values = ContentValues()
        values.put("id", inventoryPart.id)
        values.put("InventoryID", inventoryPart.InventoryID)
        values.put("TypeID", inventoryPart.TypeID)
        values.put("ItemID", inventoryPart.ItemID)
        values.put("QuantityInSet", inventoryPart.QuantityInSet)
        values.put("QuantityInStore", inventoryPart.QuantityInStore)
        values.put("ColorID", inventoryPart.ColorID)
        values.put("Extra", inventoryPart.Extra)
        dataBase?.insert("InventoriesParts", null, values)
        closeDatabase()
    }

    fun updateInventoryActive(index: Int){
        openDataBase()
        val values = ContentValues()
        values.put(COLUMN_ACTIVE, 0)
        dataBase?.update(TABLE_INVENTORIES, values, "id ="+index.toString(), null)
        closeDatabase()
    }

    fun updateInventoryPartQuantityInSet(id: Int, value : String){
        openDataBase()
        val values = ContentValues()
        values.put("QuantityInStore", value)

        dataBase?.update("InventoriesParts", values, "id ="+id.toString(), null)
        closeDatabase()
    }

    fun readInventory() : ArrayList<Inventory> {
        var inventory: Inventory
        val resultInventoryList = ArrayList<Inventory>()
        val cursor = dataBase!!.rawQuery("SELECT * FROM Inventories", null)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            inventory = Inventory(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3))
            resultInventoryList.add(inventory)
            cursor.moveToNext()
        }
        cursor.close()
        return resultInventoryList
    }

    fun readInventoryParts( index: Int){
        openDataBase()
        var inventoryPart: InventoryPart
        val resultInventoryPartList = ArrayList<InventoryPart>()
        val array = arrayOf(index.toString())
        val cursor = dataBase!!.rawQuery("SELECT * FROM InventoriesParts WHERE InventoryID = ?" , array)
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {
            inventoryPart = InventoryPart(cursor.getInt(0),
                                          cursor.getInt(1),
                                          cursor.getInt(2),
                                          cursor.getInt(3),
                                          cursor.getInt(4),
                                          cursor.getInt(5),
                                          cursor.getInt(6),
                                          cursor.getInt(7))

            resultInventoryPartList.add(inventoryPart)
            cursor.moveToNext()
        }
        cursor.close()
        closeDatabase()
        viewInventoryPartsList = resultInventoryPartList
    }
    fun getItemID(IDString : String): Int{
        openDataBase()
        val array = arrayOf(IDString)
        val cursor = dataBase!!.rawQuery("SELECT id FROM Parts WHERE Code = ?" , array)

        var id : Int = -1
        cursor.moveToFirst()
        if( cursor.count > 0){
            id = cursor.getInt(0)
        }

        cursor.close()
        closeDatabase()
        return id
    }

    fun getCodeFromParts( itemID : Int ): String{
        openDataBase()
        val array = arrayOf(itemID.toString())
        var code = ""
        val cursorCode = dataBase!!.rawQuery("SELECT Code FROM Parts WHERE id = ?" , array)

        if( cursorCode.count > 0){
            cursorCode.moveToFirst()
            code = cursorCode.getString(0)
        }
        cursorCode.close()
        closeDatabase()
        return code
    }

    fun getTypeID(IDString : String): Int{
        openDataBase()
        val array = arrayOf(IDString)
        val cursor = dataBase!!.rawQuery("SELECT id FROM ItemTypes WHERE Code = ?" , array)

        var typeID : Int = -1
        cursor.moveToFirst()
        if( cursor.count > 0){
            typeID = cursor.getInt(0)
        }

        cursor.close()
        closeDatabase()
        return typeID
    }

    fun getLastInventoryID(): Int{
        openDataBase()
        val cursor = dataBase!!.rawQuery("SELECT MAX(id) FROM Inventories" , null)

        cursor.moveToFirst()
        val maxId : Int = cursor.getInt(0)
        cursor.close()
        closeDatabase()
        return maxId
    }

    fun getLastInventoryPartID(): Int{
        openDataBase()
        val cursor = dataBase!!.rawQuery("SELECT MAX(id) FROM InventoriesParts" , null)

        cursor.moveToFirst()
        val maxId : Int = cursor.getInt(0)
        cursor.close()
        closeDatabase()
        return maxId
    }

    fun getBrickCodeForImage( itemID : Int, colorID : Int) : Int{
        openDataBase()
        val array = arrayOf(itemID.toString(), colorID.toString())
        val cursor = dataBase!!.rawQuery("SELECT Code FROM Codes WHERE ItemID = ? AND ColorID = ?" , array)
        var code : Int = -1

        cursor.moveToFirst()
        if( cursor.count > 0 ){
            code = cursor.getInt(0)
        }
        closeDatabase()
        cursor.close()
        return code
    }

    fun checkIfImageExists( code : Int ): Int{
        openDataBase()
        val array = arrayOf(code.toString())
        val cursor = dataBase!!.rawQuery("SELECT Image FROM Codes WHERE Code = ?" , array)

        cursor.moveToFirst()

        if( cursor.count > 0){
            if(cursor.getBlob(0) == null){
                cursor.close()
                closeDatabase()
                return 0
            }
            else{
                cursor.close()
                closeDatabase()
                return 1
            }
        }
        else{
            cursor.close()
            closeDatabase()
            return -1
        }
    }

    fun downloadAndAddToDBImageOfBrick( itemID: Int, colorID : Int ){

        val code : Int = getBrickCodeForImage(itemID, colorID)
        val condition : Int = checkIfImageExists(code)
        if( condition == 0 ) {
      //  if( true ) {
            val response = khttp.get(
                    url="https://www.lego.com/service/bricks/5/2/" + code.toString()
            )

            if ( response.statusCode == 200) {
                addImageToDB( code, response.content)
            }
            else{
                val secondTryResponse = khttp.get(
                        url="http://img.bricklink.com/P/" + colorID.toString() + "/" + getCodeFromParts(itemID) + ".jpg")
                if( secondTryResponse.statusCode == 200 ) {
                    addImageToDB(code, secondTryResponse.content)
                }
                else{
                    val thirdTryResponse = khttp.get(
                            url="http://img.bricklink.com/P/" + colorID.toString() + "/" + getCodeFromParts(itemID) + ".gif")
                    if( thirdTryResponse.statusCode == 200 ) {
                        addImageToDB(code, thirdTryResponse.content)
                    }
                }
            }

        }
    }

    fun addImageToDB( code : Int, byteImage : ByteArray){
        openDataBase()
        val values = ContentValues()

        values.put("Image", byteImage)
        dataBase?.update("Codes", values, "Code ="+code.toString(), null)
        closeDatabase()
    }

    fun getImageOfBrick( itemID: Int, colorID : Int ) : Bitmap?{

        val code : Int = getBrickCodeForImage(itemID, colorID)
        openDataBase()
        if( code != -1 ){

            val array = arrayOf(code.toString())
            val cursor = dataBase!!.rawQuery("SELECT Image FROM Codes WHERE Code = ?", array)
            var bMap : Bitmap

            cursor.moveToFirst()
            if( cursor.getBlob(0) != null){
                bMap = BitmapFactory.decodeByteArray(cursor.getBlob(0), 0, cursor.getBlob(0).size)
                bMap = Bitmap.createScaledBitmap(bMap, 120, 120, false)
                cursor.close()
                closeDatabase()
                return bMap
            }
            else{
                cursor.close()
                closeDatabase()
                return null
            }
        }
        else{
            closeDatabase()
            return null
        }
    }

    fun getNameOfBrick( itemID : Int, colorID : Int ): String{
        openDataBase()
        var array = arrayOf(itemID.toString())

        val cursorCode = dataBase!!.rawQuery("SELECT Code FROM Parts WHERE id = ?" , array)

        val name : String

        if( cursorCode.count > 0){
            cursorCode.moveToFirst()
            val code : String = cursorCode.getString(0)

            array = arrayOf(code)
            val cursorName = dataBase!!.rawQuery("SELECT Name FROM Parts WHERE Code = ?" , array)
            cursorName.moveToFirst()
            name = cursorName.getString(0)
            cursorName.close()
        }
        else{
            name = "Item ID = "+itemID.toString()+", ColorID = " + colorID.toString()
        }

        cursorCode.close()
        closeDatabase()
        return name
    }
}