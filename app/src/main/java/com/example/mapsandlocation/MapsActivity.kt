package com.example.mapsandlocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

import android.widget.AdapterView
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var options: ArrayList<String>
    lateinit var context: Context
    lateinit var newIcon: Bitmap
    lateinit var drawable: Drawable
    lateinit var destination : LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        context = baseContext

        // turn this into a bitm
        drawable = resources.getDrawable(R.drawable.ic_star_24dp)
        newIcon = drawableToBitmap(drawable)!!

        //Init the options
        startSpinner()
        getLocation()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //5 different map types none, normal, terrain, satellite, hybrid
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID)

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)

        displayLocationonMap(sydney, "Marker in Sydney")

    }
    //function for adding the point
    private fun displayLocationonMap(latLng: LatLng, title:String){


        //newIcon = BitmapFactory.decodeResource(context.resources,R.drawable.ic_star_24dp)

        mMap.addMarker(MarkerOptions().position(latLng).title(title)
            .icon(BitmapDescriptorFactory.fromBitmap(newIcon))
            //.icon(drawableToBitmap(R.drawable.ic_star_24dp))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }


    //geocoding
    //public LatLng getLocationByAddress
    fun getLocationByAddress(address: String) : LatLng{
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocationName(address, 1)[0]

        //return that lat and long from the geocoder
        return LatLng(addressResult.latitude, addressResult.longitude)

    }

    //Reverse Geocoding
    fun getAddressUsingLatLng(latLng: LatLng) : String{
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1).get(0)

        return addressResult.getAddressLine(0)
    }

    //using that synthetic binding boy
    fun onClick(view: View) {

        val userAddress = etUserAddress.text.toString()
        val userLatLng = etLatLng.text.toString()

        try {
            if (userAddress.isNotEmpty()) {
                var retrievedLatLng = getLocationByAddress(userAddress)
                displayLocationonMap(retrievedLatLng, getAddressUsingLatLng(retrievedLatLng))
            } else if (userLatLng.isNotEmpty()) {
                var splitLatLng = userLatLng.split(',')
                var retrievedLatLng =
                    LatLng((splitLatLng[0]).toDouble(), (splitLatLng[1]).toDouble())
                val Address = getAddressUsingLatLng(retrievedLatLng)
                displayLocationonMap(retrievedLatLng, Address)
            }
        } catch (e : IOException){
            Log.e("MAP", "Here we go", e)
        }
    }

    fun startSpinner(){
        options = ArrayList<String>()
        //5 different map types none, normal, terrain, satellite, hybrid

        options.add("Terrain")
        options.add("None")
        options.add("Normal")
        options.add("Satellite")
        options.add("Hybrid")

        var arrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnMapType.setAdapter(arrayAdapter)

        //setting the on select listener
        spnMapType.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long){
                val tutorialsName = parent.getItemAtPosition(position).toString()

                //Switch statement
                when(position){
                    0 -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    1 -> mMap.setMapType(GoogleMap.MAP_TYPE_NONE)
                    2 -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    3 -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    4 -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        })
    }


    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    //get current location
    fun getLocation() {

        var locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        var locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                var latitute = location!!.latitude
                var longitute = location!!.longitude

                Log.i("test", "Latitute: $latitute ; Longitute: $longitute")

                //setting the location based on the end bit
                destination = LatLng(latitute, longitute)
                //getAddressUsingLatLng(destination)

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {
            }

        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getLocation()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(context, "Permission required to get location", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100

    }

    fun focusOnUser(view: View){
        displayLocationonMap(destination, "Your location")
    }
    }
