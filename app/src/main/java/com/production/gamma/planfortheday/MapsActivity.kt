package com.production.gamma.planfortheday

import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import java.util.*
import android.graphics.Typeface
import android.R.attr.y
import java.lang.reflect.Type
import android.graphics.drawable.Drawable




class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var listOfCoords : Vector<LatLng>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize the list of coords
        listOfCoords = Vector<LatLng>()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        // Add a marker in Sydney and move the camera
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(10.3181466, 123.9029382), 12F))

        mMap.setOnMapClickListener {coordsPointed->
            listOfCoords.addElement(coordsPointed)
            zoomPlan()
            addToPlan(coordsPointed)
        }
/*
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=10.3181466,123.9029382&destination=10.311795,123.915864&key=AIzaSyDJuCMZsDL9duF_OgxK-K17ox8_Z3pVY0k"

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
                    mMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
        }, Response.ErrorListener {
                _ ->
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)*/

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
    }
}
