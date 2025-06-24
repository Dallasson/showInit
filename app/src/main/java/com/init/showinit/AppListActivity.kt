package com.init.showinit


import android.os.Bundle
import android.util.Log
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AppListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@AppListActivity,4)
        }
        setContentView(recyclerView)

        val deviceID = intent.getStringExtra("deviceID")
        if (deviceID == null) {
            Toast.makeText(this, "Device ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dbRef = FirebaseDatabase.getInstance().reference
            .child("Info").child(deviceID).child("apps")

        adapter = AppListAdapter(emptyList())
        recyclerView.adapter = adapter

        fetchAppList()
    }

    private fun fetchAppList() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appList = ArrayList<AppInfo>()
                for (appSnap in snapshot.children) {
                    val name = appSnap.child("name").getValue(String::class.java) ?: continue
                    val pkg = appSnap.child("package").getValue(String::class.java) ?: continue
                    val icon = appSnap.child("icon").getValue(String::class.java) ?: ""
                    val version = appSnap.child("version").getValue(String::class.java) ?: "N/A"

                    appList.add(AppInfo(name, pkg, icon, version))
                }

                adapter = AppListAdapter(appList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AppListActivity, "Failed to load apps", Toast.LENGTH_SHORT).show()
                Log.e("AppListActivity", "Firebase error: ${error.message}")
            }
        })
    }

}
