package com.br.esoterics.dev.home

import com.br.esoterics.dev.Center

/**
 * Created by vitor_juca on 28/12/17.
 */
interface HomeContract {

    interface View{
        fun showError(t: Throwable)
        fun insertCenter(center: Center)
        fun onSaveButtonClick(center: Center)
        fun onRemoveButtonClick(center: Center)
        fun removeMarker()
    }
    interface Presenter{
        fun requestAllCenters(isNetworkOnline: Boolean)
        fun requestSaveCenter(isNetworkOnline: Boolean, center: Center)
        fun requestRemoveCenter(isNetworkOnline: Boolean, center: Center)
    }
}