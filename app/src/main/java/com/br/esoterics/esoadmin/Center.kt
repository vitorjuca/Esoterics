package com.br.esoterics.esoadmin

/**
 * Created by vaniajuca on 27/10/17.
 */

import java.io.Serializable



class Center : Serializable{
    private var address: Address
    private var model: Model
    private var key: String = ""
    private var icon: String = ""

    constructor(key: String, address: Address, model: Model, icon: String){
        this.key = key
        this.address = address
        this.model = model
        this.icon = icon
    }

    fun getKey() : String{
        return this.key
    }

    fun setKey(key: String){
        this.key = key
    }

    fun getAddress() : Address {
        return this.address
    }

    fun getModel(): Model {
        return this.model
    }


}