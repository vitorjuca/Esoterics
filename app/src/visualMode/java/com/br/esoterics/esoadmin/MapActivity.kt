package com.br.esoterics.esoadmin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.*
import kotlinx.android.synthetic.visualMode.activity_map.*
import android.location.Geocoder
import android.location.Location
import com.br.esoterics.esoadmin.LocationPermissionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import java.util.*


class MapActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener {

    private var googleMap: GoogleMap? = null
    private val locationPermissionManager = LocationPermissionManager()
    private var googleApiClient: GoogleApiClient? = null
    private val myDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference()
    private val storageCenters = ArrayList<com.br.esoterics.esoadmin.Center>()
    private var lastCenter = Center("", Address(), Model(), "")
    private lateinit var lastMarker: Marker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        connectGoogleApiClient()
        log("VISUAL")


        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun showEditBox(flag: Boolean){
        if(flag){
            editBox.visibility = com.br.esoterics.esoadmin.VISIBLE
        }else{
            editBox.visibility = com.br.esoterics.esoadmin.GONE
        }
    }




    fun sendToast(data: String){
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }




    fun loadEditBoxWithInfoFrom(center: com.br.esoterics.esoadmin.Center){
        centerType.setText(center.getModel().type)
        centerName.setText(center.getModel().name)
        centerPhone.setText(center.getModel().phone)
        centerAddress.setText(center.getAddress().fullAddress)
        centerStartTime.setText(center.getModel().time_start)
        centerEndTime.setText(center.getModel().time_end)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {

        googleMap = p0

        if (googleMap != null) {
            googleMap!!.setOnMarkerClickListener(this)
            googleMap!!.setOnMapClickListener(this)
            googleMap!!.setOnMapLongClickListener(this)
            if(locationPermissionManager.isPermissionGranted(this)){
                googleMap!!.setMyLocationEnabled(true)
                googleMap!!.getUiSettings().setMyLocationButtonEnabled(true)
                getAllLocations()
            }else{
                locationPermissionManager.requestPermissions(this)
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if(marker != null){
            lastMarker = marker
            var centerDAO: com.br.esoterics.esoadmin.Center? = null
            var flag = false
            for (center: com.br.esoterics.esoadmin.Center in storageCenters){
                if(center.getKey().equals(marker.snippet)){
                    centerDAO = center
                    flag = true
                    break
                }
            }
            if(flag){
                if(centerDAO != null)
                    loadEditBoxWithInfoFrom(centerDAO)
            }

            lastCenter.setKey(marker.snippet)
        }
        showEditBox(true)
        return true
    }



    override fun onMapClick(location: LatLng?) {
        showEditBox(false)
    }

    override fun onMapLongClick(location: LatLng?) {

    }

    fun getAllLocations(){
        val dbQuery = myDatabase.child(com.br.esoterics.esoadmin.CENTERS)
        dbQuery.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.d("DATA", "CHEGOU ALGO")
                if(dataSnapshot!!.exists()){
                    if (googleMap != null) {
                        Log.d("PRE-FOR", "ENTRANDO")
                        for (center: DataSnapshot in dataSnapshot.children){
                            Log.d("DENTRO DO FOR", "Adicionando classes")
                            val key = center.key
                            Log.d("KEY",key)
                            val address = center.child(com.br.esoterics.esoadmin.ADDRESS)
                            val model = center.child(com.br.esoterics.esoadmin.MODEL)
                            val myCenter = Center(
                                    center.key,
                                    Address(street = address.child("street").value.toString(),
                                            number = address.child("number").value.toString(),
                                            city = address.child("city").value.toString(),
                                            state = address.child("state").value.toString(),
                                            country = address.child("country").value.toString(),
                                            latitude = address.child("latitude").value.toString(),
                                            longitude = address.child("longitude").value.toString(),
                                            neighborhood = address.child("neighborhood").value.toString(),
                                            fullAddress = address.child("fullAddress").value.toString()),
                                    Model(  name = model.child("name").value.toString(),
                                            phone = model.child("phone").value.toString(),
                                            time_end = model.child("time_end").value.toString(),
                                            time_start = model.child("time_start").value.toString(),
                                            type = model.child("type").value.toString()),
                                    ""
                            )
                            storageCenters.add(myCenter)

                            val centerLatitude = myCenter.getAddress().latitude.toDouble()
                            val centerLongitude = myCenter.getAddress().longitude.toDouble()
                            val centerKey = myCenter.getKey()

                            val marker = MarkerOptions()
                            marker.position(LatLng(centerLatitude, centerLongitude))
                                    .title(model.child("name").value.toString())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_icon))
                                    .flat(true)
                                    .snippet(centerKey)


                            googleMap!!.addMarker(marker)
                        }
                        Log.d("HIDE PROGRESS BAR", "HIDE")
                        //hideProgressBar()
                    }
                }
            }
        })
    }

    private fun connectGoogleApiClient(){
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)//Be aware of state of the connection
                .build()
        //Tentando conexão com o Google API. Se a tentativa for bem sucessidade, o método
        // onConnected() será chamado, senão, o método onConnectionFailed() será chamado.
        googleApiClient!!.connect()
    }

    override fun onConnected(p0: Bundle?) {


    }

    override fun onConnectionSuspended(p0: Int) {}

    fun log(string: String){
        Log.d("DEBUGGER", string)
    }

}
