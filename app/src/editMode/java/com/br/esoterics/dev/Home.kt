package com.br.esoterics.dev

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.br.esoterics.dev.helpers.isNetworkOnline
import com.br.esoterics.dev.home.HomeContract
import com.br.esoterics.dev.home.HomePresenter
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.editMode.activity_map.*

class Home : AppCompatActivity(),
        HomeContract.View,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMapClickListener{

    private val presenter by lazy { HomePresenter(this) }
    private var googleMap: GoogleMap? = null
    private val progressDialog by lazy { initProgressDialog() }
    private val locationManager by lazy { initLocationPermissionManager() }
    private val googleApiClient by lazy { initGoogleApiClient() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        progressDialog.setTitle("Carregando")
        progressDialog.setMessage("Aguarde...")
        progressDialog.setCancelable(false)

        presenter.startApp(isNetworkOnline())

    }

    override fun onStartApp() {
        connectGoogleApiClient()
        hideCenterInfo()
        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
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
            presenter.requestAllCenters(isNetworkOnline())
            hideProgressDialog()
        }
    }

    override fun onConnected(p0: Bundle?) {
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
//            presenter.logDebug(e.printStackTrace().toString())
        }
    }
    override fun onConnectionSuspended(p0: Int) {}

    override fun onMapClick(p0: LatLng?) {

    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            presenter.openEditBoxFromMarker(marker)
        }
        return true
    }

    override fun insertMarker(markerOptions: MarkerOptions) {
        if (googleMap != null) {
            googleMap!!.addMarker(markerOptions)
        }
    }

    override fun onRemoveButtonClick(center: Center) {
        presenter.requestRemoveCenter(isNetworkOnline(), center)
    }

    override fun onSaveButtonClick(center: Center) {
        presenter.requestSaveCenter(isNetworkOnline(), center)
    }

    override fun removeMarker(marker: Marker) {
        if (googleMap != null) {
            googleMap!!.clear()
        }
    }

    override fun showEditBox(center: Center) {

    }

    override fun showError(t: Throwable) {
        Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
    }

    override fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    override fun showProgressDialog() {
        progressDialog.show()
    }

    override fun hideCenterInfo() {
        //log("HIDE CENTER INFO")
        var fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        editBox.startAnimation(fadeOut)
        editBox.visibility = View.GONE
    }

    private fun connectGoogleApiClient(){
        googleApiClient.connect()
    }

    private fun initGoogleApiClient(): GoogleApiClient{
        return GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
    }

    private fun initLocationPermissionManager(): LocationPermissionManager{
        return LocationPermissionManager()
    }
    private fun initProgressDialog(): ProgressDialog {
        return ProgressDialog(this)
    }
}
