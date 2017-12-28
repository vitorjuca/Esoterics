package com.br.esoterics.dev.helpers

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by vitor_juca on 26/12/17.
 */


fun Context.isNetworkOnline(): Boolean{
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}


val defaultThrowable by lazy { Throwable("Unknown error") }
val dataThrowable by lazy { Throwable("Unable to get the data") }


