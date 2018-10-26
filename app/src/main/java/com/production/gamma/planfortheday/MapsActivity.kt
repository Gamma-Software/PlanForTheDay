package com.production.gamma.planfortheday

import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.NavigationView
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import java.util.*
import android.graphics.Typeface
import android.R.attr.y
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import java.lang.reflect.Type
import android.graphics.drawable.Drawable
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, MenuItem.OnMenuItemClickListener{


    private lateinit var mMap: GoogleMap
    private lateinit var listOfCoords : Vector<LatLng>

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun initVar(){
        // Initialize the list of coords
        listOfCoords = Vector<LatLng>()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
    private fun initViews(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initVar()
        initViews()

        nav_view.setNavigationItemSelectedListener(this)

        nav_view.menu.add(1,1,1,"test").setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if(item?.itemId == 1)
        {
            Toast.makeText(this,"test", Toast.LENGTH_LONG).show()
        }
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        /*when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)*/
        return true
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

        zoomToLocation()

        mMap.setOnMapClickListener {coordsPointed->
            listOfCoords.addElement(coordsPointed)
            zoomPlan()
            addToPlan(coordsPointed)
        }
    }

    private fun zoomToLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }else{
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }

        }
    }

    fun getApproxXToCenterText(text: String, typeface: Typeface, fontSize: Float, widthToFitStringInto: Int): Int {
        val p = Paint()
        p.typeface = typeface
        p.textSize = fontSize
        val textWidth = p.measureText(text)
        return ((widthToFitStringInto - textWidth) / 2f).toInt() - (fontSize / 2f).toInt()
    }

    private fun addToPlan(currentMarker: LatLng)
    {
        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(100, 100, conf)
        val canvas = Canvas(bmp)

        val whitePaint = Paint()
        val fontSize = resources.getDimensionPixelSize(R.dimen.fontSize)
        whitePaint.textSize = fontSize.toFloat()
        whitePaint.color = Color.WHITE

        val headerFontSize = 50F
        val header = listOfCoords.size.toString()

        val blackPaint = Paint()
        blackPaint.color = Color.BLACK

        canvas.drawCircle( 50F, 50F, 50F, blackPaint)
        canvas.drawText(header, 50F, 50F, whitePaint)

        //mMap.addMarker(MarkerOptions().position(currentMarker).icon(BitmapDescriptorFactory.fromBitmap(bmp)))
        mMap.addMarker(MarkerOptions().position(currentMarker))
    }

    private fun zoomPlan()
    {
        val bounds = LatLngBounds.Builder()
        listOfCoords.forEach {
            bounds.include(it)
        }

        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), w, h, 100))
        mMap.setMaxZoomPreference(12F)
    }
}
