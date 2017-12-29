package com.br.esoterics.dev

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.br.esoterics.dev.helpers.isNetworkOnline
import com.br.esoterics.dev.helpers.log
import com.br.esoterics.dev.home.HomeContract
import com.br.esoterics.dev.home.HomePresenter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.editMode.activity_map.*

class Home : AppCompatActivity(),
        HomeContract.View,
        OnMapReadyCallback{

    private val presenter by lazy { HomePresenter(this) }
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        presenter.requestAllCenters(isNetworkOnline())

        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(gMap: GoogleMap?) {
        if (gMap != null) {
            googleMap = gMap
        }
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
}
