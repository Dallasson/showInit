package com.init.showinit

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import kotlinx.parcelize.Parcelize
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.util.Log
import android.graphics.drawable.Drawable

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var dbRef: DatabaseReference
    private lateinit var deviceNameView: TextView
    private lateinit var deviceIdView: TextView
    private lateinit var batteryView: TextView
    private lateinit var networkTypeView: TextView
    private lateinit var appCountView: TextView
    private lateinit var appCard: View

    private var selectedDeviceId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))

        map = findViewById(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        dbRef = FirebaseDatabase.getInstance().reference.child("Info")

        deviceNameView = findViewById(R.id.deviceNameValue)
        deviceIdView = findViewById(R.id.deviceIdValue)
        batteryView = findViewById(R.id.batteryValue)
        networkTypeView = findViewById(R.id.networkTypeValue)
        appCountView = findViewById(R.id.appCountValue)
        appCard = findViewById(R.id.cardSection)

        appCard.setOnClickListener {
            selectedDeviceId?.let { id ->
                val intent = Intent(this, AppListActivity::class.java)
                intent.putExtra("deviceID", id)
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
            }
        }

        clearCardUI()
        observeDeviceUpdates()
    }

    private fun observeDeviceUpdates() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                map.overlays.clear()

                val markerIcon: Drawable? =
                    ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_device_marker)

                for (child in snapshot.children) {
                    val device = child.getValue(DeviceInfo::class.java) ?: continue
                    val point = GeoPoint(device.latitude, device.longitude)

                    val marker = Marker(map).apply {
                        position = point
                        title = device.deviceName
                        icon = markerIcon
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { marker, _ ->
                            val clickedLat = marker.position.latitude
                            val clickedLon = marker.position.longitude
                            if (clickedLat == device.latitude && clickedLon == device.longitude) {
                                Log.d("MainActivity", "Marker clicked for ${device.deviceName} at ($clickedLat, $clickedLon)")
                                updateCardUI(device)
                            }
                            true
                        }
                    }

                    map.overlays.add(marker)
                }

                map.controller.setZoom(8.0)
                snapshot.children.iterator().takeIf { it.hasNext() }?.next()?.getValue(DeviceInfo::class.java)?.let {
                    map.controller.setCenter(GeoPoint(it.latitude, it.longitude))
                }

                map.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Firebase Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Firebase error: ${error.message}")
            }
        })
    }

    private fun updateCardUI(device: DeviceInfo) {
        deviceNameView.text = device.deviceName
        deviceIdView.text = device.deviceID
        batteryView.text = "${device.battery}%"
        networkTypeView.text = device.networkType
        appCountView.text = device.apps.size.toString()
        selectedDeviceId = device.deviceID
    }

    private fun clearCardUI() {
        deviceNameView.text = ""
        deviceIdView.text = ""
        batteryView.text = ""
        networkTypeView.text = ""
        appCountView.text = ""
        selectedDeviceId = null
    }
}
