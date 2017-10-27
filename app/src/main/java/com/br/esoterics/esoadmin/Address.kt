package com.br.esoterics.esoadmin

/**
 * Created by vaniajuca on 27/10/17.
 */
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