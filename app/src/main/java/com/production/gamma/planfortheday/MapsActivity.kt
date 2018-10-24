package com.production.gamma.planfortheday

import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import java.util.*


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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(48.860170, 2.343449)))

        mMap.setOnMapClickListener {coordsPointed->
            listOfCoords.addElement(coordsPointed)
            zoomPlan()
            addToPlan(coordsPointed)
        }

    }

    private fun setTextSizeForWidth(paint: Paint, desiredWidth: Float,text: String) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        val testTextSize = 48f

        // Get the bounds of the text, using our testTextSize.
        paint.textSize = testTextSize
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // Calculate the desired size as a proportion of our testTextSize.
        val desiredTextSize = testTextSize * desiredWidth / bounds.width()

        // Set the paint for that size.
        paint.textSize = desiredTextSize
    }

    private fun addToPlan(currentMarker: LatLng)
    {
        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(100, 100, conf)
        val canvas = Canvas(bmp)

        val whitePaint = Paint()
        setTextSizeForWidth(whitePaint, 48F, listOfCoords.size.toString())
        whitePaint.color = Color.WHITE
        val blackPaint = Paint()
        blackPaint.color = Color.BLACK

        canvas.drawCircle( 50F, 50F, 50F, blackPaint)
        canvas.drawText(listOfCoords.size.toString(), 10F, 80F, whitePaint) // paint defines the text color, stroke width, size

        mMap.addMarker(MarkerOptions().position(currentMarker).icon(BitmapDescriptorFactory.fromBitmap(bmp)))
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
