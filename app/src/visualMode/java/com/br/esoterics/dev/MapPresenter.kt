package com.br.esoterics.dev

import android.view.View
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

/**
 * Created by vitorjuca on 10/12/17.
 */
class MapPresenter(val view: MapContract.View): BasePresenter(), MapContract.Presenter {


    private val firebase by lazy { initFireBase() }
    private val storageCenters: ArrayList<Center> = arrayListOf()
    private val storageMarkersOptions: ArrayList<MarkerOptions> = arrayListOf()
    private val storageMarkers: ArrayList<Marker> = arrayListOf()


    override fun startApp(isNetWorkOnline: Boolean) {
        view.showProgressDialog()
        if(isNetWorkOnline){
            view.onStartAppCallback()
        }
        else{
            view.showErrorView()
            view.dismissProgressDialog()
        }
    }

    override fun requestAllCenters() {
        val query = firebase.child("Centers")
        query.addListenerForSingleValueEvent(onValueEventListener())
    }

    override fun loadMarkerInfo(marker: Marker){
        var center = searchCenterFromMarkerKey(marker)
        view.showCenterInfo(center, getCenterDrawable(center))
    }

    override fun populateMarkersOptionsFromCenters(centersList: ArrayList<Center>) {
        centersList.forEach { center ->

            var markerOptions = MarkerOptions()
                    .position(LatLng(center.address.latitude.toDouble(),
                                     center.address.longitude.toDouble())
                             )
                    .title(center.model.name)
                    .icon(BitmapDescriptorFactory
                            .fromResource(getCenterDrawable(center)))
                    .flat(true)
                    .snippet(center.key)
            storageMarkersOptions.add(markerOptions)
        }
        view.onPopulateMarkersFromCentersCallback(storageMarkersOptions)
    }

    override fun addMarkerOptionsToGoogleMap(markerOptionsList: ArrayList<MarkerOptions>) {
        markerOptionsList.forEach { markerOption ->
            view.onAddMarkerOptionsToGoogleMapCallBack(markerOption)
        }
        view.dismissProgressDialog()
    }

    override fun populateMarkerFromGoogleMap(marker: Marker) {
            storageMarkers.add(marker)
    }

    override fun filterMarkerByType(type: String) {
        var centerFiltered = storageCenters.filter { center ->
            !center.model.type.equals(type)
        }
        storageMarkers.forEach { marker ->
            marker.isVisible = true
            if (!type.equals("Todos")){
                centerFiltered.forEach { center ->
                    logDebug(center.model.type)
                    if (center.key.equals(marker.snippet))
                        marker.isVisible = false
                }
            }
        }
    }

    override fun showProgressDialog() {
        view.showProgressDialog()
    }

    override fun dismissProgressDialog() {
        view.dismissProgressDialog()
    }

    override fun onBackPressed(state: Int) {
        if (state == View.VISIBLE){
            view.hideCenterInfo()
        }else{
            view.onBackPressedTwice()
        }
    }



    private fun searchCenterFromMarkerKey(marker: Marker): Center{
        var filterResult = storageCenters.filter { it ->
            it.key.equals(marker.snippet)
        }
        return filterResult.first()
    }

    private fun onValueEventListener() = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError?) {}

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            dataSnapshot?.children?.forEach{ data ->
                var center = Center()
                center.key = data.key
                center.address.latitude = data.child(ADDRESS).child("latitude").value.toString()
                center.address.longitude = data.child(ADDRESS).child("longitude").value.toString()
                center.model.name = data.child(MODEL).child("name").value.toString()
                center.model.type = data.child(MODEL).child("type").value.toString()
                center.model.time_start = data.child(MODEL).child("time_start").value.toString()
                center.model.time_end = data.child(MODEL).child("time_end").value.toString()
                center.model.phone = data.child(MODEL).child("phone").value.toString()
                center.address.fullAddress = data.child(ADDRESS).child("fullAddress").value.toString()
                storageCenters.add(center)
            }
            view.onRequestAllCentersCallback(storageCenters)
        }
    }

    private fun getCenterDrawable(center: Center): Int{
        var centerType = center.model.type
        if(centerType.equals("Umbanda")){
            return R.drawable.umbanda
        }else if(centerType.equals("Candomblé")){
            return R.drawable.candomble
        }else if(centerType.equals("Xamanico")){
            return R.drawable.xamanico
        }else if(centerType.equals("Esotericos")){
            return R.drawable.esotericos
        }else{
            return R.drawable.outros
        }
    }

    private fun initFireBase(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }
}