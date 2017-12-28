package com.br.esoterics.dev.home

import com.br.esoterics.dev.Center

/**
 * Created by vitor_juca on 28/12/17.
 */
class HomePresenter(val view: HomeContract.View): HomeContract.Presenter {


    private val repository by lazy { HomeRepository() }

    override fun requestAllCenters(isNetworkOnline: Boolean) {
        if (isNetworkOnline){
            repository.requestAllCenters(
                    onSuccess = {
                        it.forEach { center ->
                            view.insertCenter(center)
                        }
                    },
                    onError = {})
        }
    }

    override fun requestRemoveCenter(isNetworkOnline: Boolean, center: Center) {
        
    }

    override fun requestSaveCenter(isNetworkOnline: Boolean, center: Center) {

    }

}