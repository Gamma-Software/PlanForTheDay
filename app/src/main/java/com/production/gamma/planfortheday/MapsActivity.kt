package com.production.gamma.planfortheday

import android.graphics.*
import android.support.v7.app.AppCompatActivity
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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.maps.android.PolyUtil
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

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

        val latLngOrigin = LatLng(48.970447, 2.194971) // Tech
        val latLngOrigin2 = LatLng(48.972770, 2.190612) // Pompe fun
        val latLngOrigin3 = LatLng(48.970756, 2.195972) // auchan
        mMap.addMarker(MarkerOptions().position(latLngOrigin).title("Tech"))
        mMap.addMarker(MarkerOptions().position(latLngOrigin2).title("Pompe funebre"))
        mMap.addMarker(MarkerOptions().position(latLngOrigin3).title("Auchan"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))

        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin="+latLngOrigin.latitude.toString()+
                ","+latLngOrigin.longitude.toString()+"SA&destination="+latLngOrigin3.latitude.toString()+","+latLngOrigin3.longitude.toString()+
                "&waypoints=optimize:true|"+latLngOrigin2.latitude.toString()+","+latLngOrigin2.longitude.toString()+"&key=" + getString(R.string.google_maps_key)
        val directionsRequest = object : StringRequest(
            Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->

            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }

            for (i in 0 until path.size) {
                mMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }, Response.ErrorListener {
                _ ->
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
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
