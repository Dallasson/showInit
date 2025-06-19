package com.init.showinit

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var dbRef: DatabaseReference

    private lateinit var deviceNameView: TextView
    private lateinit var deviceIdView: TextView
    private lateinit var batteryView: TextView
    private lateinit var networkTypeView: TextView

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

        // Bind views from card layout
        deviceNameView = findViewById(R.id.deviceNameValue)
        deviceIdView = findViewById(R.id.deviceIdValue)
        batteryView = findViewById(R.id.batteryValue)
        networkTypeView = findViewById(R.id.networkTypeValue)

        // Clear all card values initially
        clearCardUI()

        observeDeviceUpdates()
    }

    private fun observeDeviceUpdates() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                map.overlays.clear()
                var focused = false

                for (child in snapshot.children) {
                    val device = child.getValue(DeviceInfo::class.java) ?: continue

                    val point = GeoPoint(device.latitude, device.longitude)
                    val marker = Marker(map).apply {
                        position = point
                        title = device.deviceName
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { marker, _ ->
                            val selected = findDeviceByLatLon(
                                marker.position.latitude,
                                marker.position.longitude,
                                snapshot
                            )
                            selected?.let {
                                updateCardUI(it)
                            }
                            true
                        }
                    }

                    map.overlays.add(marker)

                    if (!focused) {
                        map.controller.setZoom(8.0)
                        map.controller.setCenter(point)
                        // Do NOT update cards until clicked
                        focused = true
                    }
                }

                map.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Firebase Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun findDeviceByLatLon(lat: Double, lon: Double, snapshot: DataSnapshot): DeviceInfo? {
        for (child in snapshot.children) {
            val device = child.getValue(DeviceInfo::class.java)
            if (device != null && device.latitude == lat && device.longitude == lon) {
                return device
            }
        }
        return null
    }

    private fun updateCardUI(device: DeviceInfo) {
        deviceNameView.text = device.deviceName
        deviceIdView.text = device.deviceID
        batteryView.text = "${device.battery}%"
        networkTypeView.text = device.networkType
    }

    private fun clearCardUI() {
        deviceNameView.text = ""
        deviceIdView.text = ""
        batteryView.text = ""
        networkTypeView.text = ""
    }
}

data class DeviceInfo(
    val deviceID: String = "",
    val deviceName: String = "",
    val battery: Int = -1,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val networkType: String = ""
)
