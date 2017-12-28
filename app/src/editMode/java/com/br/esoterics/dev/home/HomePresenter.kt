package com.br.esoterics.dev.home

import com.br.esoterics.dev.Center
import com.br.esoterics.dev.helpers.dataSaveThrowable
import com.br.esoterics.dev.helpers.networkThrowable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Created by vitor_juca on 28/12/17.
 */
class HomePresenter(private val view: HomeContract.View): HomeContract.Presenter {


    private val repository by lazy { HomeRepository() }
    private var storageMarkers: ArrayList<Marker> = arrayListOf()
    private var storageCenters: ArrayList<Center> = arrayListOf()

    override fun requestAllCenters(isNetworkOnline: Boolean) {
        if (isNetworkOnline){
            repository.requestAllCenters(
                    onSuccess = {
                        it.forEach { center ->
                            storageCenters.add(center)
                            view.insertCenter(center)
                            view.insertMarker(initMarkerOption(center))
                        }
                    },
                    onError = {view.showError(it)})
        }else{
            view.showError(networkThrowable)
        }
    }

    override fun requestRemoveCenter(isNetworkOnline: Boolean, center: Center) {
        if (isNetworkOnline){
            repository.requestRemoveCenter(center,
                    onSuccess = { view.removeMarker(find(center) as Marker) },
                    onError = { view.showError(it) })
        }else{
            view.showError(networkThrowable)
        }
    }

    override fun requestSaveCenter(isNetworkOnline: Boolean, center: Center) {
        if (isNetworkOnline){
            if (center.key != ""){
                repository.requestSaveCenter(center,
                        onSuccess = { view.insertCenter(center) },
                        onError = { view.showError(it) })
            }else{
                view.showError(dataSaveThrowable)
            }
        }else{
            view.showError(networkThrowable)
        }
    }

    override fun openEditBoxFromMarker(marker: Marker) {
        var center = find(marker) as Center
        view.showEditBox(center)
    }

//    private fun findMarkerFromCenterKey(key: String): Marker {
//        return storageMarkers.filter{ it.snippet.equals(key) }.get(0)
//    }
//    private fun findCenterFromMarker(marker: Marker): Center{
//        return storageCenters.filter { it.key.equals(marker.snippet) }.get(0)
//    }

    private fun find(obj: Any): Any?{
        return when(obj){
            is Center -> storageMarkers.filter{ it.snippet.equals(obj.key) }.get(0)
            is Marker -> storageCenters.filter { it.key.equals(obj.snippet) }.get(0)
            else -> return null
        }
    }

    private fun initMarkerOption(center: Center): MarkerOptions{
        return MarkerOptions()
                .position(LatLng(center.address.latitude.toDouble(),
                                 center.address.longitude.toDouble()))
                .title(center.model.name)
                .flat(true)
                .snippet(center.key)
    }

}