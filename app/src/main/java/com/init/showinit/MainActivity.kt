package com.init.showinit

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import kotlinx.parcelize.Parcelize
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.core.graphics.toColorInt
import android.os.Parcelable

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var dbRef: DatabaseReference
    private lateinit var deviceNameView: TextView
    private lateinit var deviceIdView: TextView
    private lateinit var batteryView: TextView
    private lateinit var networkTypeView: TextView
    private lateinit var appCountView: TextView
    private lateinit var deviceModelView: TextView
    private lateinit var manufacturerView: TextView
    private lateinit var androidVersionView: TextView
    private lateinit var appCard: View
    private var selectedDeviceId: String? = null

    private val markerColors = listOf(
        Color.RED,
        Color.BLUE,
        Color.BLACK,
        Color.GREEN,
        "#FFA500".toColorInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))


        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        dbRef = FirebaseDatabase.getInstance().reference.child("Info")

        referenceViews()

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

    private fun referenceViews(){
        map = findViewById(R.id.mapView)
        deviceNameView = findViewById(R.id.deviceNameValue)
        deviceIdView = findViewById(R.id.deviceIdValue)
        batteryView = findViewById(R.id.batteryValue)
        networkTypeView = findViewById(R.id.networkTypeValue)
        appCountView = findViewById(R.id.appCountValue)
        deviceModelView = findViewById(R.id.deviceModelValue)
        manufacturerView = findViewById(R.id.manufacturerValue)
        androidVersionView = findViewById(R.id.androidVersionValue)
        appCard = findViewById(R.id.cardSection)

    }

    private fun observeDeviceUpdates() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                map.overlays.clear()

                for (child in snapshot.children) {
                    val device = child.getValue(DeviceInfo::class.java) ?: continue
                    val point = GeoPoint(device.latitude, device.longitude)

                    val randomColor = markerColors.random()
                    val tintedIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_device_marker)?.mutate()
                    tintedIcon?.let {
                        DrawableCompat.setTint(DrawableCompat.wrap(it), randomColor)
                    }

                    val marker = Marker(map).apply {
                        position = point
                        title = device.deviceName
                        icon = tintedIcon
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
        batteryView.text = if (device.battery >= 0) "${device.battery}%" else ""
        networkTypeView.text = device.networkType

        val count = if (device.appCount > 0) device.appCount else device.apps.size
        appCountView.text = count.toString()

        deviceModelView.text = device.deviceModel ?: ""
        manufacturerView.text = device.manufacturer ?: ""
        androidVersionView.text = device.androidVersion ?: ""
        selectedDeviceId = device.deviceID
    }

    private fun clearCardUI() {
        deviceNameView.text = ""
        deviceIdView.text = ""
        batteryView.text = ""
        networkTypeView.text = ""
        appCountView.text = ""
        deviceModelView.text = ""
        manufacturerView.text = ""
        androidVersionView.text = ""
        selectedDeviceId = null
    }

}

