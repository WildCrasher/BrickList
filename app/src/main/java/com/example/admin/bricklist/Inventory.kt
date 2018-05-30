package com.example.admin.bricklist

/**
 * Created by Admin on 2018-05-15.
 */


class Inventory{
    var id: Int = 0
    var Name: String = ""
    var Active: Int = 0
    var LastAccessed: Int = 0

    constructor(id: Int, Name: String, Active: Int, LastAccessed: Int){
        this.id = id
        this.Name = Name
        this.Active = Active
        this.LastAccessed = LastAccessed
    }
}
