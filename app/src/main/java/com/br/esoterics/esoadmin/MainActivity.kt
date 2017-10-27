package com.br.esoterics.esoadmin

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat

import android.support.v4.app.FragmentActivity;
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList
import android.widget.EditText



class MainActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener {



    private var googleMap: GoogleMap? = null
    private val locationPermissionManager = LocationPermissionManager()
    private var googleApiClient: GoogleApiClient? = null
    private var addActionCounter: Int = 0
    private val myDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference()
    private val storageCenters = ArrayList<Center>()
    private var lastCenter = Center("", Address(), Model(), "")
    private lateinit var lastMarker: Marker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectGoogleApiClient()
        makeEditBoxVisible(false)
        makeAddBoxVisible(true)
        makeImageBoxVisible(false)

        add_button.setOnClickListener {
            if(addActionCounter == 0){
                makeImageBoxVisible(true)
                addActionCounter++
            }else if(addActionCounter == 1){
                if(googleMap != null){
                    val myRef = myDatabase.push()
                    val key = myRef.key
                    val location = googleMap!!.cameraPosition.target
                    val marker = MarkerOptions().position(location)
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.ic_location_icon))
                            .snippet(key)
                    val center = Center(key,
                                        Address(latitude = location.latitude.toString(),
                                                longitude = location.longitude.toString()),
                                        Model(),"1")
                    storageCenters.add(center)
                    lastCenter = center
                    googleMap!!.addMarker(marker)

                }
                Toast.makeText(this, "Criado", Toast.LENGTH_SHORT).show()
                makeAddBoxVisible(false)
                makeImageBoxVisible(false)
                makeEditBoxVisible(true)
                makeEditBoxEditable(false)
                addActionCounter = 0
            }

        }

        edit_button.setOnClickListener {
            makeEditBoxEditable(true)

        }

        edit_save_button.setOnClickListener{
            var center = lastCenter
            center.getAddress().fullAddress = center_address.text.toString()
            center.getModel().name = center_name.text.toString()
            center.getModel().phone = center_phone.text.toString()
            center.getModel().type = tipos_spinner.selectedItem.toString()
            persistCenterOnDatabase(center)
            makeEditBoxVisible(false)
            makeAddBoxVisible(true)
            Toast.makeText(this, "Salvo", Toast.LENGTH_SHORT).show()
        }

        remove_button.setOnClickListener {
            removeCenterFromDatabase(lastCenter)
            lastMarker.remove()
            Toast.makeText(this, "Excluido", Toast.LENGTH_SHORT).show()
            makeEditBoxVisible(false)
            makeAddBoxVisible(true)

        }

        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun makeEditBoxVisible(flag: Boolean){
        if(flag){
            remove_button.visibility = View.VISIBLE
            infoBox.visibility = View.VISIBLE
            edit_button.visibility = View.VISIBLE
        }else{
            infoBox.visibility = View.GONE
            edit_button.visibility = View.GONE
            remove_button.visibility = View.GONE
        }
    }
    fun makeEditBoxEditable(flag: Boolean){
        if(flag){
            edit_box_status.setText("Edição ativada")
            edit_box_status.setTextColor(Color.GREEN)
            remove_button.isEnabled = true
            edit_save_button.isEnabled = true
            tipos_spinner.isEnabled = true
            enableEditText(center_name)
            enableEditText(center_phone)
            enableEditText(center_address)
        }else{
            edit_box_status.setText("Edição desativada")
            edit_box_status.setTextColor(Color.RED)
            remove_button.isEnabled = false
            edit_save_button.isEnabled = false
            tipos_spinner.isEnabled = false
            disableEditText(center_name)
            disableEditText(center_phone)
            disableEditText(center_address)
        }
    }

    fun disableEditText(editText: EditText) {
        editText.setEnabled(false);
        editText.setInputType(InputType.TYPE_NULL);
    }

    fun enableEditText(editText: EditText){
        editText.setEnabled(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    fun makeAddBoxVisible(flag: Boolean){
        if(flag){
            add_button.visibility = View.VISIBLE
        }else{
            //infoBox.visibility = View.GONE
            add_button.visibility = View.GONE
        }

    }

    fun makeImageBoxVisible(flag: Boolean){
        if(flag){
            image_marker.visibility = View.VISIBLE
        }else{
            image_marker.visibility = View.GONE
        }
    }

    fun persistCenterOnDatabase(center: Center){
            myDatabase.child("Centers").child(center.getKey()).setValue(center)
    }

    fun removeCenterFromDatabase(center: Center){
        myDatabase.child("Centers").child(center.getKey()).removeValue()
    }

    fun loadEditBoxWithInfoFrom(center: Center){
        center_name.setText(center.getModel().name)
        center_phone.setText(center.getModel().phone)
        center_address.setText(center.getAddress().fullAddress)
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
        makeEditBoxEditable(false)
        if(marker != null){
            lastMarker = marker
            var centerDAO: Center? = null
            var flag = false
            for (center: Center in storageCenters){
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
        makeEditBoxVisible(true)
        makeAddBoxVisible(false)
        makeImageBoxVisible(false)
        return true
    }



    override fun onMapClick(location: LatLng?) {
        makeAddBoxVisible(true)
        makeEditBoxVisible(false)
        makeImageBoxVisible(false)
        addActionCounter = 0
    }

    override fun onMapLongClick(location: LatLng?) {

    }

    fun getAllLocations(){
        val dbQuery = myDatabase.child(CENTERS)
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
                            val address = center.child(ADDRESS)
                            val model = center.child(MODEL)
                            val myCenter = Center(
                                    center.key,
                                    Address(street = address.child("street").value.toString(),
                                            number = address.child("number").value.toString(),
                                            city = address.child("city").value.toString(),
                                            state = address.child("state").value.toString(),
                                            country = address.child("country").value.toString(),
                                            latitude = address.child("latitude").value.toString(),
                                            longitude = address.child("longitude").value.toString(),
                                            neighborhood = address.child("neighborhood").value.toString()),
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
        //Tentando conexão com o Google API. Se a tentativa for bem sucessidade, o método onConnected() será chamado, senão, o método onConnectionFailed() será chamado.
        googleApiClient!!.connect()
    }

    override fun onConnected(p0: Bundle?) {}

    override fun onConnectionSuspended(p0: Int) {}

    fun log(string: String){
        Log.d("DEBUGGER", string)
    }

}
