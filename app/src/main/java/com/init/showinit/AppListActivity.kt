package com.init.showinit

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AppListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_list)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.appRecyclerView)

        val layoutManager = GridLayoutManager(this, 4)
        recyclerView.layoutManager = layoutManager

        val deviceID = intent.getStringExtra("deviceID")
        if (deviceID == null) {
            Toast.makeText(this, "Device ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dbRef = FirebaseDatabase.getInstance().reference
            .child("Info").child(deviceID).child("apps")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categorizedMap = LinkedHashMap<String, MutableList<AppInfo>>()

                for (appSnap in snapshot.children) {
                    val name = appSnap.child("name").getValue(String::class.java) ?: continue
                    val pkg = appSnap.child("package").getValue(String::class.java) ?: continue
                    val icon = appSnap.child("icon").getValue(String::class.java) ?: ""
                    val version = appSnap.child("version").getValue(String::class.java) ?: "N/A"
                    val category = appSnap.child("category").getValue(String::class.java) ?: "Other"

                    val installTime = appSnap.child("installTime").getValue(String::class.java) ?: "0"
                    val updateTime = appSnap.child("updateTime").getValue(String::class.java) ?: "0"
                    val screenTime = appSnap.child("screenTime").getValue(String::class.java) ?: "0"

                    val app = AppInfo(name, pkg, icon, version, category, installTime, updateTime, screenTime)
                    categorizedMap.getOrPut(category) { mutableListOf() }.add(app)
                }

                val items = buildList {
                    categorizedMap.forEach { (category, apps) ->
                        add(category)
                        addAll(apps)
                    }
                }

                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (items[position] is String) 4 else 1
                    }
                }

                adapter = AppListAdapter(items)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AppListActivity, "Failed to load apps", Toast.LENGTH_SHORT).show()
                Log.e("AppListActivity", "Firebase error: ${error.message}")
            }
        })
    }
}