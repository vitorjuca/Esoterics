package com.br.esoterics.dev

/**
 * Created by vaniajuca on 27/10/17.
 */

import java.io.Serializable



data class Center(
        var address: Address = Address(),
        var model: Model = Model(),
        var key: String = "",
        var icon: String = "",
        var isPersisted: String = "0") : Serializable



data class Address(
        var street: String = "0",
        var number: String = "0",
        var city: String = "0",
        var state: String = "0",
        var country: String = "0",
        var latitude: String = "0",
        var longitude: String = "0",
        var neighborhood: String = "0",
        var fullAddress: String = "0"
)

data class Model(
        var name: String = "0",
        var phone: String = "0",
        var time_end: String = "0",
        var time_start: String = "0",
        var type: String = "0"
)