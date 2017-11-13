package com.br.esoterics.esoadmin
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.*
import kotlinx.android.synthetic.visualMode.activity_map.*
import com.br.esoterics.esoadmin.network.ApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
    private val myDatabase: DatabaseReference = FirebaseDatabase.getInstance()
                                                                .getReference()
    private val storageCenters = ArrayList<Center>()
    private val storageMarkers = ArrayList<Marker>()
    private val storageMarkersOptionsManager = ArrayList<MarkerOptions>()
    private var lastCenter = Center("", Address(), Model(), "")
    private var lastMarker: Marker? = null
    private var myLastLocation: Location? = null

    var fm = fragmentManager
    var spinnerDialog = MySpinnerDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        spinnerDialog.show(fm, "tag")
        connectGoogleApiClient()
        log("VISUAL")
        showEditBox(false)

        centerTypeFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, arg3: Long) {
                var type = parent!!.getItemAtPosition(position).toString()
                filterGoogleMapsBy(type)
            }
        }

        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun showEditBox(flag: Boolean){
        if(flag){
            if(editBox.visibility != VISIBLE)
                editBox.visibility = VISIBLE
        }else{
            if(editBox.visibility != GONE)
                editBox.visibility = GONE
        }
    }

    fun filterGoogleMapsBy(string: String){
        spinnerDialog.show(fm, "tag")

        googleMap!!.clear()
        if (string.equals("Umbanda")) {
            for (marker: MarkerOptions in storageMarkersOptionsManager) {
                storageCenters.forEach { center ->
                    if (center.getKey().equals(marker.snippet)
                            && center.getModel().type.equals(string)) {
                        googleMap!!.addMarker(marker)
                    }
                }
            }
        }else if (string.equals("Candomblé")) {
            for (marker: MarkerOptions in storageMarkersOptionsManager) {
                storageCenters.forEach { center ->
                    if (center.getKey().equals(marker.snippet)
                            && center.getModel().type.equals(string)) {
                        googleMap!!.addMarker(marker)
                    }
                }
            }
        }else if (string.equals("Xamanico")) {
            for (marker: MarkerOptions in storageMarkersOptionsManager) {
                storageCenters.forEach { center ->
                    if (center.getKey().equals(marker.snippet)
                            && center.getModel().type.equals(string)) {
                        googleMap!!.addMarker(marker)
                    }
                }
            }
        }else if (string.equals("Esotericos")) {
            for (marker: MarkerOptions in storageMarkersOptionsManager) {
                storageCenters.forEach { center ->
                    if (center.getKey().equals(marker.snippet)
                            && center.getModel().type.equals(string)) {
                        googleMap!!.addMarker(marker)
                    }
                }
            }
        }else if (string.equals("Outro")) {
            for (marker: MarkerOptions in storageMarkersOptionsManager) {
                storageCenters.forEach { center ->
                    if (center.getKey().equals(marker.snippet)
                            && center.getModel().type.equals(string)) {
                        googleMap!!.addMarker(marker)
                    }
                }
            }
        }else if (string.equals("Todos")) {
            for (marker: MarkerOptions in storageMarkersOptionsManager) {
                googleMap!!.addMarker(marker)

            }
        }
        spinnerDialog.dismiss()
    }


    fun sendToast(data: String){
        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
    }




    fun loadEditBoxWithInfoFrom(center: Center){
        centerType.setText(center.getModel().type)
        centerName.setText(center.getModel().name)
        centerPhone.setText(center.getModel().phone)
        centerAddress.setText(center.getAddress().fullAddress)
        centerStartTime.setText(center.getModel().time_start)
        centerEndTime.setText(center.getModel().time_end)
        centerTypeImg.background = getDrawable(getCenterDrawable(center))
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
                spinnerDialog.dismiss()
            }else{
                locationPermissionManager.requestPermissions(this)
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        spinnerDialog.show(fm, "tag")
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
        spinnerDialog.dismiss()
        return true
    }



    override fun onMapClick(location: LatLng?) {
        showEditBox(false)
    }

    override fun onMapLongClick(location: LatLng?) {

    }

    fun findRoute(origin: LatLng, destination: LatLng) {
        val origin = "${origin.latitude},${origin.longitude}"
        val destination = "${destination.latitude},${destination.longitude}"

        val call = ApiClient.apiService().fetchRouteToTheCenter(origin, destination)
        call.enqueue(object: Callback<String> {

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                TODO("not implemented") //To change body of created functions
                // use File | Settings | File Templates.
            }

            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                val result = JSONObject(response.toString())
                var routes = result.getJSONArray("routes")
                var distance = routes.getJSONObject(0)
                                        .getJSONArray("legs")
                                        .getJSONObject(0)
                                        .getJSONObject("distance")
                                        .getInt("value")

            }

        })
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
                                    Address(street = address.child("street")
                                                    .value.toString(),
                                            number = address.child("number")
                                                    .value.toString(),
                                            city = address.child("city")
                                                    .value.toString(),
                                            state = address.child("state")
                                                    .value.toString(),
                                            country = address.child("country")
                                                    .value.toString(),
                                            latitude = address.child("latitude")
                                                    .value.toString(),
                                            longitude = address.child("longitude")
                                                    .value.toString(),
                                            neighborhood = address.child("neighborhood")
                                                    .value.toString(),
                                            fullAddress = address.child("fullAddress")
                                                    .value.toString()),
                                    Model(name = model.child("name")
                                                .value.toString(),
                                            phone = model.child("phone")
                                                    .value.toString(),
                                            time_end = model.child("time_end")
                                                    .value.toString(),
                                            time_start = model.child("time_start")
                                                    .value.toString(),
                                            type = model.child("type")
                                                    .value.toString()),
                                    ""
                            )
                            storageCenters.add(myCenter)

                            val centerLatitude = myCenter.getAddress().latitude.toDouble()
                            val centerLongitude = myCenter.getAddress().longitude.toDouble()
                            val centerKey = myCenter.getKey()
                            log("Key:" + centerKey)
                            log("Latitude:" + centerLatitude.toString())
                            log("Longitude:" + centerLongitude.toString())
                            val marker = MarkerOptions()
                            marker.position(LatLng(centerLatitude, centerLongitude))
                                    .title(model.child("name").value.toString())
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(getCenterDrawable(myCenter)))
                                    .flat(true)
                                    .snippet(centerKey)
                            val markerMap = googleMap!!.addMarker(marker)
                            storageMarkers.add(markerMap)
                            storageMarkersOptionsManager.add(marker)
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
        try {
            log("MYLASTLOCATION")
            myLastLocation = LocationServices
                    .FusedLocationApi
                    .getLastLocation(googleApiClient)
            log(myLastLocation!!.latitude.toString())
            log(myLastLocation!!.longitude.toString())
            if(myLastLocation != null){
                log(myLastLocation!!.latitude.toString())
                log(myLastLocation!!.longitude.toString())
                googleMap!!.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(LatLng(myLastLocation!!.latitude,
                        myLastLocation!!.longitude),15F))
            }

        }catch (e: SecurityException){}

    }

    override fun onConnectionSuspended(p0: Int) {}

    fun log(string: String){
        Log.d("DEBUGGER", string)
    }

    fun getCenterDrawable(center: Center): Int{
        var centerType = center.getModel().type
        if(centerType.equals("Umbanda")){
            return R.drawable.umbanda
        }else if(centerType.equals("Candomblé")){
            return R.drawable.candomble
        }else if(centerType.equals("Xamanico")){
            return R.drawable.xamanico
        }else if(centerType.equals("Esotericos")){
            return R.drawable.esotericos
        }else{
            return R.drawable.outros
        }
    }

}
