package com.example.admin.bricklist

/**
 * Created by Admin on 2018-05-17.
 */
class InventoryPart {
    var id: Int = 0
    var InventoryID: Int = 0
    var TypeID: Int = 0
    var ItemID: Int = 0
    var QuantityInSet: Int = 0
    var QuantityInStore: Int = 0
    var ColorID: Int = 0
    var Extra: Int = 0

    constructor(id: Int,
                InventoryID: Int,
                TypeID: Int,
                ItemID: Int,
                QuantityInSet: Int,
                QuantityInStore: Int,
                ColorID: Int,
                Extra: Int)
    {
            this.id = id
            this.InventoryID = InventoryID
            this.TypeID = TypeID
            this.ItemID = ItemID
            this.QuantityInSet = QuantityInSet
            this.QuantityInStore = QuantityInStore
            this.ColorID = ColorID
            this.Extra = Extra
    }
    constructor(){}
}