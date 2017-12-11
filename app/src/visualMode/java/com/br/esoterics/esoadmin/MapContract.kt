package com.br.esoterics.esoadmin

import com.br.dev.vj.Center
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


/**
 * Created by vitorjuca on 10/12/17.
 */
interface MapContract {

    interface Presenter{
        fun requestAllCenters()
        fun populateMarkersOptionsFromCenters(centersList: ArrayList<Center>)
        fun populateMarkerFromGoogleMap(marker: Marker)
        fun addMarkerOptionsToGoogleMap(markerOptionsList: ArrayList<MarkerOptions>)
        fun filterMarkerByType(type: String)
        fun loadMarkerInfo(marker: Marker)
        fun onBackPressed(state: Int)
    }
    interface View{
        fun showCenterInfo(center: Center, centerDrawable: Int)
        fun hideCenterInfo()
        fun onRequestAllCentersCallback(centersList: ArrayList<Center>)
        fun onPopulateMarkersFromCentersCallback(markerOptionsList: ArrayList<MarkerOptions>)
        fun onAddMarkerOptionsToGoogleMapCallBack(markerOption: MarkerOptions)
        fun onBackPressedTwice()
        fun clearGoogleMap()
    }

}