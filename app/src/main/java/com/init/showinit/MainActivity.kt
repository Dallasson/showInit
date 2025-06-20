package com.init.showinit

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private var selectedAppList: ArrayList<AppInfo>? = null

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

        // Bind views
        deviceNameView = findViewById(R.id.deviceNameValue)
        deviceIdView = findViewById(R.id.deviceIdValue)
        batteryView = findViewById(R.id.batteryValue)
        networkTypeView = findViewById(R.id.networkTypeValue)
        appCountView = findViewById(R.id.appCountValue)
        appCard = findViewById(R.id.appCountCard)

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

                for (child in snapshot.children) {
                    val device = child.getValue(DeviceInfo::class.java) ?: continue
                    val point = GeoPoint(device.latitude, device.longitude)

                    val marker = Marker(map).apply {
                        position = point
                        title = device.deviceName
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { marker, _ ->
                            val clickedLat = marker.position.latitude
                            val clickedLon = marker.position.longitude

                            // Check if this marker matches this device's location
                            if (clickedLat == device.latitude && clickedLon == device.longitude) {
                                Log.d("MainActivity", "Marker clicked for ${device.deviceName} at ($clickedLat, $clickedLon)")

                                updateCardUI(device)

                                val apps = ArrayList<AppInfo>()
                                child.child("apps").children.forEach { appSnap ->
                                    val name = appSnap.child("name").getValue(String::class.java) ?: return@forEach
                                    val pkg = appSnap.child("package").getValue(String::class.java) ?: return@forEach
                                    val icon = appSnap.child("icon").getValue(String::class.java) ?: ""
                                    apps.add(AppInfo(name, pkg, icon))
                                }

                                Log.d("MainActivity", "Loaded ${apps.size} apps for selected device.")
                                selectedAppList = apps
                            }

                            true
                        }
                    }

                    map.overlays.add(marker)
                }

                map.controller.setZoom(8.0)
                if (snapshot.children.iterator().hasNext()) {
                    val firstDevice = snapshot.children.iterator().next().getValue(DeviceInfo::class.java)
                    firstDevice?.let {
                        map.controller.setCenter(GeoPoint(it.latitude, it.longitude))
                    }
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
        selectedAppList = null
    }
}
