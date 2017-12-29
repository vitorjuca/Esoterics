package com.br.esoterics.dev

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.view.View
import android.view.animation.*
import android.widget.AdapterView
import android.widget.Spinner
import com.google.android.gms.maps.*
import kotlinx.android.synthetic.editMode.activity_map.*
import java.util.*


class MapActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        LocationListener{

    private var googleMap: GoogleMap? = null
    private val locationPermissionManager = LocationPermissionManager()
    private var googleApiClient: GoogleApiClient? = null
    private val myDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val storageCenters = ArrayList<Center>()
    private var lastCenter = Center()
    private var lastCenterCheck: Boolean = false
    private var lastMarker: Marker? = null
    private var lastMkOp = MarkerOptions()
    var fm = fragmentManager
    var instance = MySpinnerDialog()
    private var myLastLocation: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        connectGoogleApiClient()

        showMenuBox(true)

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
            log(type)
            centerTypeImg.background = resources.getDrawable(getMipmapFromString(type))
        }
    }

    fun onRemoveButton() = View.OnClickListener {
        if(lastCenter.key != ""){
            removeCenterFromDatabase(lastCenter)
            lastMarker!!.remove()
            showEditBox(false)
            showMenuBox(true)
        }else{
            sendToast("Local ainda não foi salvo")
        }
    }

    fun onSaveButton() = View.OnClickListener {
        if(validateEditBox()){
            var center = lastCenter
            center.address.fullAddress = centerAddress.text.toString()
            center.model.name = centerName.text.toString()
            center.model.phone = centerPhone.text.toString()
            center.model.type = centerType.selectedItem.toString()
            center.model.time_start = centerStartTime.selectedItem.toString()
            center.model.time_end = centerEndTime.selectedItem.toString()
            center.address.fullAddress = centerAddress.text.toString()
            log(center.model.name)
            if(googleMap != null){
                if(center.key != ""){
                    persistCenterOnDatabase(center)
                    editStorageCenter(center)
                    if(lastMarker != null)
                        lastMarker!!.setIcon(BitmapDescriptorFactory.fromResource(getCenterMipmap(center)))
                }else{
                    val key = myDatabase.push().key
                    center.key = key
                    lastMarker = googleMap!!.addMarker(lastMkOp
                            .icon(BitmapDescriptorFactory.fromResource(getCenterMipmap(center)))
                            .flat(true)
                            .snippet(key))
                    lastCenter = center
                    storageCenters.add(center)
                    persistCenterOnDatabase(center)
                    if(lastMarker != null)
                        lastMarker!!.setIcon(BitmapDescriptorFactory.fromResource(getCenterMipmap(center)))

                }
            }

        }
    }

    fun persistCenterOnDatabase(center: Center){
        var dialog = MySpinnerDialog()
        dialog.show(fm, "tg")
        frame_general.isClickable = false
        myDatabase.child("Centers")
                .child(center.key)
                .setValue(center)
                .addOnCompleteListener {
                    sendToast("Salvo")
                    dialog.dismiss()
                    frame_general.isClickable = true
                    clearEditTexts()
                    showEditBox(false)
                    showMenuBox(true) }
                .addOnFailureListener {
                    sendToast("Problema ao salvar")
                    dialog.dismiss()
                    frame_general.isClickable = true }
    }

    fun removeCenterFromDatabase(center: Center){
        var dialog = MySpinnerDialog()
        dialog.show(fm, "tg")
        myDatabase.child("Centers").child(center.key).removeValue()
                .addOnCompleteListener {
                    sendToast("Excluido")
                    dialog.dismiss()
                    clearEditTexts()
                    showEditBox(false)
                    showMenuBox(true) }
                .addOnFailureListener {
                    sendToast("Problema ao excluir")
                    dialog.dismiss() }
    }

    fun editStorageCenter(center: Center){
        for(storageCenter: Center in storageCenters){
            if(center.key.equals(storageCenter.key)){
                storageCenters.remove(storageCenter)
                storageCenters.add(center)
                break
            }
        }
    }

    fun onSwitchButton() = View.OnClickListener {
        if(switchButton.isChecked)
            makeEditBoxEditable(true)
        else{
            makeEditBoxEditable(false)
        }
    }

    fun onAddOnMap() = View.OnClickListener {
        showMarkerBox(true)
    }

    fun onMarkerPositionSetted() = View.OnClickListener{
        showMarkerBox(false)
        if (googleMap != null) {
            val dialog = MySpinnerDialog()
            dialog.show(fm, "Set marker")
            var locationPointer = googleMap!!.cameraPosition.target
            if(validateCoordinates(locationPointer)){
                var address = getAddressFromLocation(locationPointer)
                val center = Center()
                center.address.latitude = locationPointer.latitude.toString()
                center.address.longitude = locationPointer.longitude.toString()
                center.address.fullAddress = address

                lastCenter = center
                lastMkOp.position(locationPointer)
                centerAddress.setText(address)
                makeSwitchChecked(true)
                makeEditBoxEditable(true)
                showEditBox(true)
                dialog.dismiss()
            }else{
                sendToast("Erro ao capturar coordenadas")
                dialog.dismiss()
            }

        }
    }

    fun getAddressFromLocation(location: LatLng): String{

        var address = ""
        try{

            val geocoder = Geocoder(this, Locale.getDefault())
            var addressesList: List<android.location.Address>

            addressesList = geocoder.getFromLocation(location.latitude, location.longitude,1)
            address = addressesList.get(0).thoroughfare
                    .plus(", ")
                    .plus(addressesList.get(0).featureName.toString())

            return address

        }catch (e: Exception){}

        return address
    }

    override fun onBackPressed() {
        if(editBox.visibility == VISIBLE){
            showEditBox(false)
            showMenuBox(true)
        }else{
            super.onBackPressed()
        }
    }

    fun getProgressDialog(): ProgressDialog{
        var progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Aguarde...")
        progressDialog.setCancelable(false)
        return progressDialog
    }

    fun validateEditBox(): Boolean{
        var flag = true
        if(centerAddress.text.toString().equals("")){
            centerAddress.setError("Endereço necessário")
            flag = false
        }
        if(centerName.text.toString().equals("")){
            centerName.setError("Nome necessário")
            flag = false
        }
        if (centerPhone.text.toString().equals("")){
            centerPhone.setError("Telefone necessário")
            flag = false
        }
        if(centerAddress.text.toString().equals("")){
            centerAddress.setError("Endereço necessário")
            flag = false
        }
        return flag
    }

    fun validateCoordinates(location: LatLng) :Boolean{
        if(location.latitude == 0.0 && location.longitude == 0.0){
            return false
        }
        return true
    }


    fun showEditBox(flag: Boolean){
        if(flag){
            if(editBox.visibility == GONE){
                editBox.startAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_fade_in))
                editBox.visibility = VISIBLE
                showMarkerBox(false)
                showMenuBox(false)
            }
        }else{
            if(editBox.visibility == VISIBLE){
                editBox.startAnimation(AnimationUtils.loadAnimation(this,R.anim.abc_fade_out))
                editBox.visibility = GONE
                clearEditTexts()
                makeSwitchChecked(false)
            }
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
            removeButton.background = resources.getDrawable(R.drawable.box_remove)
            saveButton.background = resources.getDrawable(R.drawable.box_save)
            removeButton.setTextColor(Color.WHITE)
            saveButton.setTextColor(Color.WHITE)
            removeButton.isEnabled = true
            saveButton.isEnabled = true
            centerType.isEnabled = true
            centerStartTime.isEnabled = true
            centerEndTime.isEnabled = true
            enableEditText(centerName)
            enableEditText(centerPhone)
            enableEditText(centerAddress)
        }else{
            makeSwitchChecked(false)
            removeButton.background = resources.getDrawable(R.drawable.non_editable)
            saveButton.background = resources.getDrawable(R.drawable.non_editable)
            removeButton.setTextColor(Color.LTGRAY)
            saveButton.setTextColor(Color.LTGRAY)
            removeButton.isEnabled = false
            saveButton.isEnabled = false
            centerType.isEnabled = false
            centerStartTime.isEnabled = false
            centerEndTime.isEnabled = false
            disableEditText(centerName)
            disableEditText(centerPhone)
            disableEditText(centerAddress)
        }
    }

    fun disableEditText(editText: EditText) {
        editText.setEnabled(false);
    }

    fun enableEditText(editText: EditText){
        editText.setEnabled(true);
    }



    fun loadEditBoxWithInfoFrom(center: Center){
        centerType.setSelection(getSpinnerId(centerType,center.model.type))
        centerName.setText(center.model.name)
        centerPhone.setText(center.model.phone)
        centerAddress.setText(center.address.fullAddress)
        centerStartTime.setSelection(getSpinnerId(centerStartTime,center.model.time_start))
        centerEndTime.setSelection(getSpinnerId(centerEndTime,center.model.time_end))
    }

    fun getSpinnerId(spinner: Spinner,string: String): Int{
        for (index in 0 .. spinner.adapter.count) {
            if(spinner.getItemAtPosition(index).toString().equals(string)){
                log(spinner.getItemAtPosition(index).toString())
                log(index.toString())
               return index
            }
        }
        return 0
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0

        if (googleMap != null) {
            googleMap!!.setOnMarkerClickListener(this)
            googleMap!!.setOnMapClickListener(this)
            googleMap!!.setOnMapLongClickListener(this)
            instance.show(fm, "tags")
            frame_general.isClickable = false
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
                if(center.key.equals(marker.snippet)){
                    centerDAO = center
                    flag = true
                    break
                }
            }
            if(flag){
                if(centerDAO != null)
                    loadEditBoxWithInfoFrom(centerDAO)
            }

            lastCenter.key = marker.snippet
        }
        showEditBox(true)
        return true
    }



    override fun onMapClick(location: LatLng?) {
        if(editBox.visibility == GONE){
            addButtonMenu.collapse()
            showMenuBox(true)
            if(lastMarker != null){
                if(lastMarker!!.snippet.isNullOrBlank()){
                    lastMarker!!.remove()
                }
            }
        }


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
                            val myCenter = Center()
                            myCenter.key = key
                            myCenter.address.latitude = center.child(ADDRESS).child("latitude").value.toString()
                            myCenter.address.longitude = center.child(ADDRESS).child("longitude").value.toString()
                            myCenter.model.name = center.child(MODEL).child("name").value.toString()
                            myCenter.model.type = center.child(MODEL).child("type").value.toString()
                            myCenter.model.time_start = center.child(MODEL).child("time_start").value.toString()
                            myCenter.model.time_end = center.child(MODEL).child("time_end").value.toString()
                            myCenter.model.phone = center.child(MODEL).child("phone").value.toString()
                            myCenter.address.fullAddress = center.child(ADDRESS).child("fullAddress").value.toString()

                            storageCenters.add(myCenter)

                            val centerLatitude = myCenter.address.latitude.toDouble()
                            val centerLongitude = myCenter.address.longitude.toDouble()
                            val centerKey = myCenter.key

                            val marker = MarkerOptions()
                            marker.position(LatLng(centerLatitude, centerLongitude))
                                    .title(model.child("name").value.toString())
                                    .icon(BitmapDescriptorFactory.fromResource(getCenterMipmap(myCenter)))
                                    .flat(true)
                                    .snippet(centerKey)

                            lastMarker = googleMap!!.addMarker(marker)

                        }
                        instance.dismiss()
                        frame_general.isClickable = true
                        Log.d("HIDE PROGRESS BAR", "HIDE")
                        //hideProgressBar()
                    }
                }else{
                    instance.dismiss()
                }
            }
        })
    }

    private fun connectGoogleApiClient(){
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)//Be aware of state of the connection
                .build()
//        instance.show(fm,"tag")
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

            if(myLastLocation != null){
                log(myLastLocation!!.latitude.toString())
                log(myLastLocation!!.longitude.toString())
                googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(myLastLocation!!.latitude,
                        myLastLocation!!.longitude),15F))
            }

        }catch (e: SecurityException){}
    }
    override fun onConnectionSuspended(p0: Int) {}

    fun log(string: String){
        Log.d("DEBUGGER", string)
    }

    override fun onLocationChanged(location: Location?) {

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    fun getCenterMipmap(center: Center): Int{
        var centerType = center.model.type
        if(centerType.equals("Umbanda")){
            return R.mipmap.umbanda
        }else if(centerType.equals("Candomblé")){
            return R.mipmap.candomble
        }else if(centerType.equals("Xamanico")){
            return R.mipmap.xamanico
        }else if(centerType.equals("Esotericos")){
            return R.mipmap.esotericos
        }else{
            return R.mipmap.outros
        }
    }

    fun getMipmapFromString(string: String): Int{
        var centerType = string
        if(centerType.equals("Umbanda")){
            return R.mipmap.umbanda
        }else if(centerType.equals("Candomblé")){
            return R.mipmap.candomble
        }else if(centerType.equals("Xamanico")){
            return R.mipmap.xamanico
        }else if(centerType.equals("Esotericos")){
            return R.mipmap.esotericos
        }else{
            return R.mipmap.outros
        }
    }


}
