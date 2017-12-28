package com.br.esoterics.dev.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

/**
 * Created by vitor_juca on 26/12/17.
 */


fun Context.isNetworkOnline(): Boolean{
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun Context.log(msg: String){
    Log.d("Debug", msg)
}


val defaultThrowable by lazy { Throwable("Unknown error") }
val dataThrowable by lazy { Throwable("Unable to get the data") }
val dataSaveThrowable by lazy { Throwable("Unable to save the data") }
val deleteThrowable by lazy { Throwable("Unable to get the data") }
val networkThrowable by lazy { Throwable("No Internet connection") }


