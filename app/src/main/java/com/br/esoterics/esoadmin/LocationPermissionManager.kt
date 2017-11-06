package com.br.esoterics.esoadmin



/**
 * Created by vaniajuca on 26/10/17.
 */

import android.support.v4.app.ActivityCompat
import android.app.Activity
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED


class LocationPermissionManager {


    fun requestPermissions(activity: Activity) {
        val permissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        ActivityCompat.requestPermissions(activity, permissions, com.br.esoterics.esoadmin.REQUEST_CODE_PERMISSIONS)
    }

    fun isPermissionGranted(activity: Activity): Boolean {
        return isPermissionGranted(activity, ACCESS_FINE_LOCATION) && isPermissionGranted(activity, ACCESS_COARSE_LOCATION)
    }

    private fun isPermissionGranted(activity: Activity, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED
    }

}