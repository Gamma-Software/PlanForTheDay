package com.production.gamma.planfortheday

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.Animatable
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.transition.Fade
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap

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
    private var menuState = MENU_STATE.CLOSE

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
        fadeIn = AnimationUtils.loadAnimation( this, R.anim.fade)
        fadeOut = AnimationUtils.loadAnimation( this, R.anim.fade_out)

        fadeOut.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                when(menuState){
                    MENU_STATE.OPEN->{
                        button.setImageResource(R.drawable.ic_check_white_24dp)
                        button.startAnimation(fadeIn)
                    }
                    MENU_STATE.CLOSE->{
                    }
                    MENU_STATE.CHECK->{
                        button.setImageResource(R.drawable.ic_add_white_24dp)
                        button.startAnimation(fadeIn)
                    }
                }
            }
        })
        fadeIn.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                when(menuState){
                    MENU_STATE.OPEN->{
                        menuState = MENU_STATE.CHECK
                    }
                    MENU_STATE.CLOSE->{
                    }
                    MENU_STATE.CHECK->{
                        menuState = MENU_STATE.CLOSE
                    }
                }
            }
        })

        openRotateAddPlanButton.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                button1.visibility = View.VISIBLE
                button2.visibility = View.VISIBLE
            }
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                button1.isClickable = true
                button2.isClickable = true
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
                if(menuState == MENU_STATE.CHECK){
                    button.setImageResource(R.drawable.ic_check_white_24dp)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when(v)
        {
            button-> {
                when(menuState){
                    MENU_STATE.OPEN->{
                        closeMenu()
                        menuState = MENU_STATE.CLOSE
                    }
                    MENU_STATE.CLOSE->{
                        openMenu()
                        menuState = MENU_STATE.OPEN
                    }
                    MENU_STATE.CHECK->{
                        button.startAnimation(fadeOut)
                    }
                }
            }
            button1->{
                closeMenu()
                button.startAnimation(fadeOut)
            }
            button2->{
                closeMenu()
                button.startAnimation(fadeOut)
            }
        }
    }

    private fun openMenu(){
        button.startAnimation(openRotateAddPlanButton)
        button1.startAnimation(openAddPlanItem)
        button2.startAnimation(openAddPlanItem)
    }

    private fun closeMenu(){
        button.startAnimation(closeRotateAddPlanButton)
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

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        mMap.setOnMapClickListener { p0 -> Log.d("Map", p0.toString())
            mMap.addMarker(MarkerOptions().position(p0).title("marker")) }
    }


}
