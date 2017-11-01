package com.br.esoterics.esoadmin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

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
    private val myDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference()
    private val storageCenters = ArrayList<Center>()
    private var lastCenter = Center("", Address(), Model(), "")
    private lateinit var lastMarker: Marker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectGoogleApiClient()

        showMenuBox(true)

        addOnMapButton.setOnClickListener{
            showMarkerBox(true)
        }

        addMarkerButton.setOnClickListener{
            if(googleMap != null){
                val myRef = myDatabase.push()
                val key = myRef.key
                val location = googleMap!!.cameraPosition.target
                val marker = MarkerOptions().position(location)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_icon))
                        .snippet(key)
                val center = Center(key,
                                    Address(latitude = location.latitude.toString(),
                                            longitude = location.longitude.toString()),
                                    Model(),"1")
                storageCenters.add(center)
                lastCenter = center
                googleMap!!.addMarker(marker)
            }
            makeSwitchChecked(true)
            makeEditBoxEditable(true)
            showEditBox(true)
        }

        switchButton.setOnClickListener {
            if(switchButton.isChecked)
                makeEditBoxEditable(true)
            else{
                makeEditBoxEditable(false)
            }
        }

        saveButton.setOnClickListener{
            var center = lastCenter
            center.getAddress().fullAddress = centerAddress.text.toString()
            center.getModel().name = centerName.text.toString()
            center.getModel().phone = centerPhone.text.toString()
            center.getModel().type = centerType.selectedItem.toString()
            persistCenterOnDatabase(center)
            clearEditTexts()
            showEditBox(false)
            showMenuBox(true)
        }

        removeButton.setOnClickListener {
            removeCenterFromDatabase(lastCenter)
            lastMarker.remove()
            showEditBox(false)
            showMenuBox(true)
        }

        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    
    fun showEditBox(flag: Boolean){
        if(flag){
            editBox.visibility = VISIBLE
            showMarkerBox(false)
            showMenuBox(false)
        }else{
            editBox.visibility = GONE
            clearEditTexts()
            makeSwitchChecked(false)
        }
    }

    fun showMenuBox(flag: Boolean){
        if(flag){
            addButtonMenu.collapse()
            addButtonMenu.visibility = VISIBLE
            showEditBox(false)
            showMarkerBox(false)
        }else{
            addButtonMenu.collapse()
            addButtonMenu.visibility = GONE
        }
    }

    fun showMarkerBox(flag: Boolean){
        if(flag){
            imageMarker.visibility = VISIBLE
            addMarkerButton.visibility = VISIBLE
            showEditBox(false)
            showMenuBox(false)
        }else{
            imageMarker.visibility = GONE
            addMarkerButton.visibility = GONE
        }
    }

    fun sendToast(data: String){
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }

    fun clearEditTexts(){
        centerName.setText("")
        centerPhone.setText("")
        centerAddress.setText("")
        centerWorktime.setText("")
        centerType.prompt = "Tipos"
    }
    

    fun makeSwitchChecked(flag: Boolean){
        if(flag)
            switchButton.isChecked = true
        else
            switchButton.isChecked = false
    }

    fun makeEditBoxEditable(flag: Boolean){
        if(flag){
            removeButton.background = getDrawable(R.drawable.box_remove)
            saveButton.background = getDrawable(R.drawable.box_save)
            removeButton.setTextColor(Color.WHITE)
            saveButton.setTextColor(Color.WHITE)
            removeButton.isEnabled = true
            saveButton.isEnabled = true
            centerType.isEnabled = true
            enableEditText(centerWorktime)
            enableEditText(centerName)
            enableEditText(centerPhone)
            enableEditText(centerAddress)
        }else{
            makeSwitchChecked(false)
            removeButton.background = getDrawable(R.drawable.non_editable)
            saveButton.background = getDrawable(R.drawable.non_editable)
            removeButton.setTextColor(Color.LTGRAY)
            saveButton.setTextColor(Color.LTGRAY)
            removeButton.isEnabled = false
            saveButton.isEnabled = false
            centerType.isEnabled = false
            disableEditText(centerWorktime)
            disableEditText(centerName)
            disableEditText(centerPhone)
            disableEditText(centerAddress)
        }
    }

    fun disableEditText(editText: EditText) {
        editText.setEnabled(false);
//        editText.setInputType(InputType.TYPE_NULL);
    }

    fun enableEditText(editText: EditText){
        editText.setEnabled(true);
//        editText.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    fun persistCenterOnDatabase(center: Center){
            myDatabase.child("Centers").child(center.getKey()).setValue(center)
    }

    fun removeCenterFromDatabase(center: Center){
        myDatabase.child("Centers").child(center.getKey()).removeValue()
    }

    fun loadEditBoxWithInfoFrom(center: Center){
        centerName.setText(center.getModel().name)
        centerPhone.setText(center.getModel().phone)
        centerAddress.setText(center.getAddress().fullAddress)
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
        showEditBox(true)
        return true
    }



    override fun onMapClick(location: LatLng?) {
        addButtonMenu.collapse()
        showMenuBox(true)
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
        //Tentando conexão com o Google API. Se a tentativa for bem sucessidade, o método
        // onConnected() será chamado, senão, o método onConnectionFailed() será chamado.
        googleApiClient!!.connect()
    }

    override fun onConnected(p0: Bundle?) {}

    override fun onConnectionSuspended(p0: Int) {}

    fun log(string: String){
        Log.d("DEBUGGER", string)
    }

}
