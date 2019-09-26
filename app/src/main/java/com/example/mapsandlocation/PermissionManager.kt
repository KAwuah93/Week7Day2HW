package com.example.mapsandlocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PermissionManager {
    lateinit var manager : IPermissionManager
    lateinit var context : Context
    val PERMISSION_INDEX = 101


    constructor(context: Context) {
        this.context = context
    }


    fun checkForPermissions(){

//        context = con
//
//        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(context,
//                Manifest.permission.READ_CONTACTS)
//            != PackageManager.PERMISSION_GRANTED) {
//
//            // Permission is not granted
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(contact,
//                    Manifest.permission.READ_CONTACTS)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(context,
//                    arrayOf(Manifest.permission.READ_CONTACTS),
//                    PERMISSION_INDEX)
//
//
//            }
//        } else {
//            manager.onPermissionResult(true)
//        }

    }

    fun requestPermssion(){

    }

    fun permissionResult(){

    }

    interface IPermissionManager{
        fun onPermissionResult(isGranted : Boolean)
    }
}