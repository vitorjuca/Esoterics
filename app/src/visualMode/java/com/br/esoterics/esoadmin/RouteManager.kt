package com.br.esoterics.esoadmin.visualmode

import com.google.android.gms.maps.model.LatLng

/**
 * Created by vaniajuca on 06/11/17.
 */
class RouteManager{

    fun getRoute(origin: LatLng, destination: LatLng){
        var url = ("http://maps.googleapis.com/maps/api/directions/json?origin="
                + origin.latitude + "," + origin.longitude + "&destination="
                + destination.latitude + "," + destination.longitude + "&sensor=false")


    }
}