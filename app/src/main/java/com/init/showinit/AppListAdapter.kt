package com.init.showinit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(private val apps: List<AppInfo>) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.appIcon)
        val nameView: TextView = view.findViewById(R.id.appName)
        val packageView: TextView = view.findViewById(R.id.appPackage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_vertical, parent, false)
        return AppViewHolder(view)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        // Decode Base64 to Bitmap
        if (app.iconBase64.isNotEmpty()) {
            try {
                val decodedBytes = android.util.Base64.decode(app.iconBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.iconView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.iconView.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        } else {
            holder.iconView.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        holder.nameView.text = app.name
        holder.packageView.text = app.packageName
    }
}
