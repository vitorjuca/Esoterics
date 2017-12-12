package com.br.esoterics.dev

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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