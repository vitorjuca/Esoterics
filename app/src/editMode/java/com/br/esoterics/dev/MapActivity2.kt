package com.br.esoterics.dev

import android.app.ProgressDialog
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.editMode.activity_map.*

/**
 * Created by vitorjuca on 12/12/17.
 */

class MapActivity2: AppCompatActivity(),
            MapContract.View,
            OnMapReadyCallback,
            GoogleMap.OnMarkerClickListener,
            ActivityCompat.OnRequestPermissionsResultCallback,
            GoogleApiClient.ConnectionCallbacks,
            GoogleMap.OnMapClickListener,
            GoogleMap.OnMapLongClickListener,
            LocationListener
{

    private val googleApiClient by lazy { initGoogleApiClient() }
    private val mapPresenter by lazy { initMapPresenter() }
    private val progressDialog by lazy { initProgressDialog() }
    private var googleMap: GoogleMap? = null

    override fun onStartAppCallback() {
        connectGoogleApiClient()
        hideCenterInfo()

        addOnMapButton.setOnClickListener(onAddOnMap())

        addMarkerButton.setOnClickListener(onMarkerPositionSetted())

        centerType.onItemSelectedListener = onSpinnerSelected()

        switchButton.setOnClickListener(onSwitchButton())

        saveButton.setOnClickListener(onSaveButton())

        removeButton.setOnClickListener(onRemoveButton())

        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onSpinnerSelected() = object: AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(p0: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, arg3: Long) {
            var type = parent!!.getItemAtPosition(position).toString()
            mapPresenter.onSwitchButton(type)
//            centerTypeImg.background = resources.getDrawable(getMipmapFromString(type))
        }
    }

    private fun onSaveButton() = View.OnClickListener {
        mapPresenter.onSaveButton()
    }

    private fun onSwitchButton() = View.OnClickListener {
        mapPresenter.onSwitchButton()
    }

    private fun onRemoveButton() = View.OnClickListener {
        mapPresenter.onRemoveButton()
    }

    private fun onMarkerPositionSetted() = View.OnClickListener {
        mapPresenter.onMarkerPositionSetted()
    }

    private fun onAddOnMap() = View.OnClickListener {
        mapPresenter.onAddOnMap()
    }

    override fun showToast(data: String) {
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }

    override fun showErrorView() {
    }

    override fun showCenterInfo(center: Center, centerDrawable: Int) {

    }

    override fun hideCenterInfo() {
        var fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out)
        editBox.startAnimation(fadeOut)
        editBox.visibility = View.GONE
    }

    override fun onRequestAllCentersCallback(centersList: ArrayList<Center>) {
    }

    override fun onPopulateMarkersFromCentersCallback(markerOptionsList: ArrayList<MarkerOptions>) {
    }

    override fun onAddMarkerOptionsToGoogleMapCallBack(markerOption: MarkerOptions) {
    }

    override fun onBackPressedTwice() {
        mapPresenter.onBackPressed(editBox.visibility)
    }

    override fun clearGoogleMap() {
    }

    override fun showProgressDialog() {
    }

    override fun dismissProgressDialog() {
    }

    override fun onMapReady(p0: GoogleMap?) {
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return true
    }

    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onMapClick(p0: LatLng?) {
    }

    override fun onMapLongClick(p0: LatLng?) {
    }

    override fun onLocationChanged(p0: Location?) {
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
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

    private fun initMapPresenter(): MapPresenter{
        return MapPresenter(this)
    }

    fun initProgressDialog(): ProgressDialog {
        return ProgressDialog(this)
    }

}