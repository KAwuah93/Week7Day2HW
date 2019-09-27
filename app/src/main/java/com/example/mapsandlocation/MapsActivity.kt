package com.example.mapsandlocation

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

import android.widget.AdapterView
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var options: ArrayList<String>
    lateinit var context: Context
    lateinit var newIcon: Bitmap
    lateinit var drawable: Drawable
    lateinit var destination : LatLng
    //Geo-fencing boys
    lateinit var geofencingClient: GeofencingClient
    lateinit var geofenceList: ArrayList<Geofence>
    lateinit var geoFenceMarker : Marker
    lateinit var geoFenceLimits : Circle
    //lateinit var geofencePendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        geofenceList = ArrayList()
        addtoGeo(LatLng(-34.0, 151.0), 30f)
        //Geo-fencing
        geofencingClient = LocationServices.getGeofencingClient(this)
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(this, MapsActivity::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        context = baseContext

        // turn this into a bitmap
        drawable = resources.getDrawable(R.drawable.ic_star_24dp)
        newIcon = drawableToBitmap(drawable)!!

        //Init the options
        startSpinner()
        getLocation()

    }

    fun geofenceLocation(){
        //reusing this
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(this, MapsActivity::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        addtoGeo(destination, 500f)

        geofencingClient.addGeofences(getGeofencingRequest(),geofencePendingIntent)?.run {
            addOnSuccessListener{

                Toast.makeText(context, "Added geo-fencing for the current location", Toast.LENGTH_LONG).show()
                drawGeofence()
            }
            addOnFailureListener {
                //log this
            }
        }



    }

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
                val retrievedLatLng = getLocationByAddress(userAddress)
                displayLocationonMap(retrievedLatLng, getAddressUsingLatLng(retrievedLatLng))
            } else if (userLatLng.isNotEmpty()) {
                val splitLatLng = userLatLng.split(',')
                val retrievedLatLng =
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
    private fun getLocation() {

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                val latitude = location?.latitude
                val longitude = location?.longitude

                Log.i("test", "Latitude: $latitude ; Longitude: $longitude")

                //setting the location based on the end bit
                destination = LatLng(latitude!!, longitude!!)
                //getAddressUsingLatLng(destination)


            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {}

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
                PackageManager.PERMISSION_GRANTED -> {getLocation(); mMap.isMyLocationEnabled = true}
                PackageManager.PERMISSION_DENIED -> Toast.makeText(context, "Permission required to get location", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100

    }

    fun focusOnUser(view: View){
        displayLocationonMap(destination, "Your location")
        geofenceLocation()
    }

    fun addtoGeo(latLng: LatLng, radius : Float){
        //geo fence boys
        geofenceList.add(Geofence.Builder()
            .setRequestId("001")
            .setCircularRegion(
                latLng.latitude,
                latLng.longitude,
                radius
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setLoiteringDelay(3000)
            .setExpirationDuration(NEVER_EXPIRE)
            .build()
        )


    }

    //controlling the events of the geo-fence
    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }
            .build()
    }


// Draw Geofence circle on GoogleMap
private fun drawGeofence() {
    Log.d("GEOFENCE", "drawGeofence()")

    //figure out null check
    //if ( geoFenceLimits != null ) geoFenceLimits.remove()

    //GeofenceMarker
    geoFenceMarker = mMap.addMarker(MarkerOptions().position(destination).title("Current Location")
        .icon(BitmapDescriptorFactory.fromBitmap(newIcon)))

    val circleOptions =  CircleOptions()
            .center( geoFenceMarker.getPosition())
            .strokeColor(Color.argb(50, 70,70,70))
            .fillColor( Color.argb(100, 150,150,150) )
            .radius( 30.0 )
    geoFenceLimits = mMap.addCircle( circleOptions )
}
}
