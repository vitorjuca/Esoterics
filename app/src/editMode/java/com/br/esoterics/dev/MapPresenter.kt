package com.br.esoterics.dev

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

/**
 * Created by vitorjuca on 11/12/17.
 */
class MapPresenter(val view: MapContract.View ): BasePresenter(), MapContract.Presenter {

    private val firebase by lazy { initFireBase() }
    private val storageCenters: ArrayList<Center> = arrayListOf()
    private val storageMarkersOptions: ArrayList<MarkerOptions> = arrayListOf()
    private val storageMarkers: ArrayList<Marker> = arrayListOf()

    override fun startApp(isNetWorkOnline: Boolean) {
        view.showProgressDialog()
        if (isNetWorkOnline){
            view.onStartAppCallback()
        }else{
            view.showErrorView()
            view.dismissProgressDialog()
        }
    }

    override fun requestAllCenters() {
        val query = firebase.child("Centers")
        query.addListenerForSingleValueEvent(onValueEventListener())
    }

    private fun onValueEventListener() = object : ValueEventListener {
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

    override fun populateMarkersOptionsFromCenters(centersList: ArrayList<Center>) {
    }

    override fun populateMarkerFromGoogleMap(marker: Marker) {
    }

    override fun addMarkerOptionsToGoogleMap(markerOptionsList: ArrayList<MarkerOptions>) {
    }

    override fun filterMarkerByType(type: String) {
    }

    override fun loadMarkerInfo(marker: Marker) {
    }

    override fun onBackPressed(state: Int) {
    }

    override fun showProgressDialog() {
    }

    override fun dismissProgressDialog() {
    }

    override fun onSaveButton() {
    }

    override fun onSwitchButton() {
    }

    override fun onSpinnerSelected(data: String) {
        getDrawableFromString(data)
    }

    override fun onRemoveButton() {
    }

    override fun onMarkerPositionSetted() {
    }

    override fun onAddOnMap() {
    }

    override fun getDrawableFromString(data: String) : Int{
        return when(data){
            "Umbanda" -> R.drawable.umbanda
            "Candomblé" -> R.drawable.candomble
            "Xamanico" -> R.drawable.xamanico
            "Esotericos" -> R.drawable.esotericos
            else -> R.drawable.outros

        }
    }

    private fun initFireBase(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }
}