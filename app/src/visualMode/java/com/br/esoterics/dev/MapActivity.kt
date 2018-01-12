package com.br.esoterics.dev


import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.visualMode.activity_map.*


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
    private val progressDialog by lazy { initProgressDialog() }
    private var googleMap: GoogleMap? = null
    private val locationManager by lazy { initLocationPermissionManager() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        progressDialog.setTitle("Carregando")
        progressDialog.setMessage("Aguarde...")
        progressDialog.setCancelable(false)

        mapPresenter.startApp(isNetworkOnline())
    }
    override fun onStartAppCallback() {
        log(" START APP CALLBACK")
        connectGoogleApiClient()
        hideCenterInfo()
        centerTypeFilter.onItemSelectedListener = onItemSelectedListener()

        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun showErrorView() {
        showToast("Sem conexão com internet")
    }

    override fun onMapReady(gMap: GoogleMap?) {
        googleMap = gMap
        if (googleMap != null){
            googleMap!!.setOnMapClickListener(this)
            googleMap!!.setOnMarkerClickListener(this)
            if(locationManager.isPermissionGranted(this)) {
                googleMap!!.setMyLocationEnabled(true)
                googleMap!!.getUiSettings().setMyLocationButtonEnabled(true)
            }
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

    override fun showCenterInfo(center: Center, centerDrawable: Int) {
        var fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        editBox.startAnimation(fadeIn)
        centerType.text = center.model.type
        centerName.text = center.model.name
        centerPhone.text = center.model.phone
        centerAddress.text = center.address.fullAddress
        centerStartTime.text = center.model.time_start
        centerEndTime.text = center.model.time_end
        centerTypeImg.background = resources.getDrawable(centerDrawable)
        editBox.visibility = View.VISIBLE
    }

    override fun hideCenterInfo() {
        log("HIDE CENTER INFO")
        var fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        editBox.startAnimation(fadeOut)
        editBox.visibility = View.GONE
    }

    override fun onMapClick(p0: LatLng?) {
        hideCenterInfo()
    }

    override fun onBackPressed() {
        mapPresenter.onBackPressed(editBox.visibility)
    }

    override fun onBackPressedTwice(){
        super.onBackPressed()
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    override fun clearGoogleMap() {
        if (googleMap != null) {
            googleMap!!.clear()
        }
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

    override fun onConnectionSuspended(p0: Int) {}

    fun initGoogleApiClient(): GoogleApiClient{
        return GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
    }

    fun initProgressDialog(): ProgressDialog {
        return ProgressDialog(this)
    }

    private fun initLocationPermissionManager(): LocationPermissionManager{
        return LocationPermissionManager()
    }


    fun onItemSelectedListener() = object : AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(p0: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, arg3: Long) {
            var type = parent!!.getItemAtPosition(position).toString()
            mapPresenter.filterMarkerByType(type)
        }
    }

    override fun showToast(data: String) {
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }

    fun log(string: String){
        Log.d("DEBUG", string)
    }
}
