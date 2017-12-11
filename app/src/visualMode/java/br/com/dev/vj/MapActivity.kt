package br.com.dev.vj

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.br.dev.vj.Center
import com.br.esoterics.esoadmin.GONE
import com.br.esoterics.esoadmin.R
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.visualMode.activity_map.*



/**
 * Created by vaniajuca on 10/12/17.
 */
class MapActivity: AppCompatActivity(),
        MapContract.View,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMapClickListener
{
    private val mapPresenter by lazy { MapPresenter(this) }
    private val googleApiClient by lazy { initGoogleApiClient() }
    private var googleMap: GoogleMap? = null





    override fun onMapReady(gMap: GoogleMap?) {
        googleMap = gMap
        if (googleMap != null){
            mapPresenter.requestAllCenters()
        }
    }

    override fun onRequestAllCentersCallback(centersList: ArrayList<Center>) {
        mapPresenter.populateMarkersOptionsFromCenters(centersList)
    }

    override fun onPopulateMarkersFromCentersCallback(markerOptionsList: ArrayList<MarkerOptions>) {
        mapPresenter.addMarkerOptionsToGoogleMap(markerOptionsList)
    }

    override fun onAddMarkerOptionsToGoogleMapCallBack(markerOption: MarkerOptions) {
        if (googleMap != null) {
            var marker = googleMap!!.addMarker(markerOption)
            mapPresenter.populateMarkerFromGoogleMap(marker)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null)
            mapPresenter.loadMarkerInfo(marker)
        return true
    }

    override fun showCenterInfo(center: Center) {
        centerType.text = center.model.type
        centerName.text = center.model.name
        centerPhone.text = center.model.phone
        centerAddress.text = center.address.fullAddress
        centerStartTime.text = center.model.time_start
        centerEndTime.text = center.model.time_end
        editBox.visibility = View.VISIBLE
    }

    override fun hideCenterInfo() {
        editBox.visibility = View.GONE
    }

    override fun onMapClick(p0: LatLng?) {

    }


    override fun onBackPressed() {
        mapPresenter.onBackPressed(editBox.visibility)
    }

    override fun onBackPressedTwice(){
        super.onBackPressed()
    }

    fun connectGoogleApiClient(){
        googleApiClient.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        try {
            var lastLocation = LocationServices
                                .FusedLocationApi
                                .getLastLocation(googleApiClient)
            if (lastLocation != null) {
                googleMap!!.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(LatLng(lastLocation.latitude,
                                              lastLocation.longitude),
                                              15F))
            }

        }catch (e: SecurityException){
            mapPresenter.logDebug(e.printStackTrace().toString())
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    fun initGoogleApiClient(): GoogleApiClient{
        return GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
    }

}