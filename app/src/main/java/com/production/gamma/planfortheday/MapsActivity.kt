package com.production.gamma.planfortheday

import android.support.v7.app.AppCompatActivity
import android.support.design.widget.NavigationView
import android.os.Bundle

import java.util.*
import android.R.attr.y
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.*
import java.lang.reflect.Type
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.DrawerLayout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, MenuItem.OnMenuItemClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var mMenu: DrawerLayout
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
        private const val PLACE_PICKER_REQUEST = 3
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

        // Add Listener onto the add plan button
        button  = findViewById(R.id.add_plan)
        button1 = findViewById(R.id.add_plan_address)
        button2 = findViewById(R.id.add_plan_point)
        button.setOnClickListener(this)
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)

        mMenu = findViewById(R.id.drawer_layout)
        nav_view.setNavigationItemSelectedListener(this)
    }
    private fun initAnimation(){
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initVar()
        initViews()
        initAnimation()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                addToPlan(place.latLng)
            }
        }
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
                nextState = MENU_STATE.CLOSE
                closeMenu(true)
                loadPlacePicker()
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

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        var currentMarkerIndex = 1
        for(marker in listOfCoords)
        {
            if(item?.itemId == currentMarkerIndex)
            {
                goToLocation(marker)
                Toast.makeText(this,"Marker " + currentMarkerIndex + " touched", Toast.LENGTH_SHORT).show()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 12f))
                // close drawer when item is tapped
                mMenu.closeDrawers()
            }
            currentMarkerIndex++
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
        mMap.setMaxZoomPreference(14F)
        zoomToLocation()

        mMap.setOnMapClickListener {coordsPointed->
            if(currentState == MENU_STATE.CHECK){
                addToPlan(coordsPointed)
            }else if(currentState == MENU_STATE.OPEN){
                closeMenu(true)
            }else{
                Toast.makeText(this,"Click on the + button to add", Toast.LENGTH_SHORT).show()
            }
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

    private fun goToLocation(location: LatLng)
    {
        val gmmIntentUri = Uri.parse("google.navigation:q="+location.latitude.toString()+","+location.longitude.toString())
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
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
        listOfCoords.addElement(currentMarker)

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

        val itemToAdd = nav_view.menu.add(1,listOfCoords.size,1,"GoTo Marker " + listOfCoords.size.toString())
        itemToAdd.setOnMenuItemClickListener(this)
        itemToAdd.setIcon(R.drawable.ic_place_black_24dp)

        zoomPlan()
    }

    private fun zoomPlan()
    {
        val bounds = LatLngBounds.Builder()
        listOfCoords.forEach {
            bounds.include(it)
        }

        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), w, h, 80))
    }
    private fun loadPlacePicker() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        val builder = PlacePicker.IntentBuilder()
        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }
}
