package com.br.esoterics.dev.helpers

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by vitorjuca on 11/12/17.
 */


fun Context.isNetWorkOnline(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}
