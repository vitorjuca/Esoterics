package com.br.esoterics.dev

import android.util.Log

/**
 * Created by vaniajuca on 11/12/17.
 */
open class BasePresenter {

    fun logDebug(data: String){
        Log.d("Debug", data)
    }
}