package com.production.gamma.planfortheday

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var listOfCoords : Vector<LatLng>

    private lateinit var button: FloatingActionButton
    private lateinit var button1: FloatingActionButton
    private lateinit var button2: FloatingActionButton

    private lateinit var openAddPlanItem: Animation
    private lateinit var closeAddPlanItem: Animation
    private lateinit var openRotateAddPlanButton: Animation
    private lateinit var closeRotateAddPlanButton: Animation
    private lateinit var fadeIn: Animation
    private lateinit var fadeOut: Animation

    enum class MENU_STATE{
        OPEN, CLOSE, CHECK
    }
    private var currentState = MENU_STATE.CLOSE
    private var nextState = MENU_STATE.CLOSE

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Add Listener onto the add plan button
        button  = findViewById(R.id.add_plan)
        button1 = findViewById(R.id.add_plan_address)
        button2 = findViewById(R.id.add_plan_point)
        button.setOnClickListener(this)
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)


        openAddPlanItem = AnimationUtils.loadAnimation( this, R.anim.open_add_button)
        closeAddPlanItem = AnimationUtils.loadAnimation( this, R.anim.close_add_button)
        openRotateAddPlanButton = AnimationUtils.loadAnimation( this, R.anim.open_rotation)
        closeRotateAddPlanButton = AnimationUtils.loadAnimation( this, R.anim.close_rotation)


        openRotateAddPlanButton.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                button1.visibility = View.VISIBLE
                button2.visibility = View.VISIBLE
            }
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                button1.isClickable = true
                button2.isClickable = true
                when(nextState){
                    MENU_STATE.OPEN->{
                        currentState = MENU_STATE.OPEN
                    }
                    MENU_STATE.CLOSE->{
                        currentState = MENU_STATE.CLOSE
                    }
                }
            }
        })
        closeRotateAddPlanButton.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                button1.isClickable = false
                button2.isClickable = false
            }
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                button1.visibility = View.GONE
                button2.visibility = View.GONE
                when(nextState){
                    MENU_STATE.CLOSE->{
                        button.setImageResource(R.drawable.ic_add_white_24dp)
                        currentState = MENU_STATE.CLOSE
                    }
                    MENU_STATE.OPEN->{
                        currentState = MENU_STATE.OPEN
                    }
                    MENU_STATE.CHECK->{
                        button.setImageResource(R.drawable.ic_check_white_24dp)
                        currentState = MENU_STATE.CHECK
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when(v)
        {
            button-> {
                when(currentState){
                    MENU_STATE.OPEN->{
                        nextState = MENU_STATE.CLOSE
                        closeMenu(true)
                    }
                    MENU_STATE.CLOSE->{
                        nextState = MENU_STATE.OPEN
                        openMenu()
                    }
                    MENU_STATE.CHECK->{
                        nextState = MENU_STATE.CLOSE
                        closeMenu(false)
                    }
                }
            }
            button1->{
                nextState = MENU_STATE.CHECK
                closeMenu(true)
            }
            button2->{
                nextState = MENU_STATE.CHECK
                closeMenu(true)
            }
        }
    }

    private fun openMenu(){
        button.startAnimation(openRotateAddPlanButton)
        button1.startAnimation(openAddPlanItem)
        button2.startAnimation(openAddPlanItem)
    }

    private fun closeMenu(enableAnimation: Boolean){
        if(enableAnimation)
            button.startAnimation(closeRotateAddPlanButton)
        else if(nextState == MENU_STATE.CLOSE){
            button.setImageResource(R.drawable.ic_add_white_24dp)
            currentState = MENU_STATE.CLOSE
        }
        button1.startAnimation(closeAddPlanItem)
        button2.startAnimation(closeAddPlanItem)
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
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        mMap.setOnMapClickListener { p0 -> Log.d("Map", p0.toString())
            mMap.addMarker(MarkerOptions().position(p0).title("marker")) }
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
