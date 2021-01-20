package com.example.travelbook

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var locationManager : LocationManager? = null
    var locationListener : LocationListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener(myListener)

        locationManager = getSystemService(Context.LOCATION_SERVICE)as LocationManager
        locationListener = object : LocationListener{

            override fun onLocationChanged(location: Location?) {



                if(location!= null) {
                    var useLocation = LatLng(location!!.latitude, location!!.longitude)
                    mMap.addMarker(MarkerOptions().position(useLocation).title("your location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(useLocation,15f))

                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onProviderEnabled(provider: String?) {
                TODO("Not yet implemented")
            }

            override fun onProviderDisabled(provider: String?) {
                TODO("Not yet implemented")
            }

        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)

        }else{
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)

            val intent = intent
            val info = intent.getStringExtra("info")

            if (info.equals("new")){

                mMap.clear()
                val lastLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                var lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,17f))



            }else{

                mMap.clear()
                val latitude = intent.getDoubleExtra("latitude",0.0)
                val longitude = intent.getDoubleExtra("latitude",0.0)
                val name = intent.getStringExtra("name")
                val location = LatLng(latitude,longitude)
                mMap.addMarker(MarkerOptions().position(location).title(name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15f))


            }


        }

    }
    val myListener = object : GoogleMap.OnMapLongClickListener {
        override fun onMapLongClick(p0: LatLng?) {
            val geocoder = Geocoder(applicationContext, Locale.getDefault())

            var address = " "

            try{
                
               val addressList = geocoder.getFromLocation(p0!!.latitude,p0!!.longitude,1)

                if(addressList != null && addressList.size>0){
                    if (addressList[0].thoroughfare!= null){
                        address+= addressList[0].thoroughfare

                        if(addressList[0].subThoroughfare != null){
                            address += addressList[0].subThoroughfare
                        }
                    }
                }else{
                    address = "New Place"
                }
                
            }catch (e : Exception){
                e.printStackTrace()
            }

            mMap.addMarker(MarkerOptions().position(p0!!).title(address))

            namesArray.add(address)
            locationArray.add(p0)
            Toast.makeText(applicationContext, "New Place Added", Toast.LENGTH_SHORT).show()


            try{
                val latitude = p0.latitude.toString()
                val longitude = p0.longitude.toString()

                val dataBase = openOrCreateDatabase("Travel",Context.MODE_PRIVATE,null)

                dataBase.execSQL("CREATE TABLE IF NOT EXISTS Travel (name VARCHAR,latitude VARCHAR,longitude VARCHAR)")

                val toCompile = "INSERT INTO travel (name,latitude,longitude) VALUES(?,?,?)"

                val sqlite = dataBase.compileStatement(toCompile)

                sqlite.bindString(1,address)
                sqlite.bindString(2,latitude)
                sqlite.bindString(3,longitude)

                sqlite.execute()

            }catch (e : Exception){
                e.printStackTrace()
            }

        }

    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults.isNotEmpty()){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}