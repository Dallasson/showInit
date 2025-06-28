package com.init.showinit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class AppListAdapter(private val items: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_HEADER else TYPE_APP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_category_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_vertical, parent, false)
            AppViewHolder(view)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(items[position] as String)
        } else if (holder is AppViewHolder) {
            holder.bind(items[position] as AppInfo)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.headerText)
        fun bind(title: String) {
            headerText.text = title
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.appIcon)
        private val nameView: TextView = itemView.findViewById(R.id.appName)
        private val versionView: TextView = itemView.findViewById(R.id.appVersion)

        fun bind(app: AppInfo) {
            if (app.iconBase64.isNotEmpty()) {
                try {
                    val bytes = Base64.decode(app.iconBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    iconView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    iconView.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            } else {
                iconView.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            nameView.text = app.name
            "v${app.versionName.ifEmpty { "N/A" }}".also { versionView.text = it }

            itemView.setOnClickListener {
                showAppDetailsBottomSheet(it.context, app)
            }
        }

        @SuppressLint("InflateParams")
        private fun showAppDetailsBottomSheet(context: Context, app: AppInfo) {
            val dialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_app_details, null)

            val icon = view.findViewById<ImageView>(R.id.bottomIcon)
            val name = view.findViewById<TextView>(R.id.bottomName)
            val pkg = view.findViewById<TextView>(R.id.bottomPackage)
            val version = view.findViewById<TextView>(R.id.bottomVersion)
            val install = view.findViewById<TextView>(R.id.bottomInstall)
            val update = view.findViewById<TextView>(R.id.bottomUpdate)
            val screen = view.findViewById<TextView>(R.id.bottomScreenTime)


            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())


            try {
                val bytes = Base64.decode(app.iconBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                icon.setImageBitmap(bitmap)
            } catch (e: Exception) {
                icon.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            // Set values
            name.text = app.name
            "Package: ${app.packageName}".also { pkg.text = it }
            "Version: ${app.versionName}".also { version.text = it }

            val installTime = app.installTime.toLongOrNull()
            val updateTime = app.updateTime.toLongOrNull()
            val screenTime = app.screenTime.toLongOrNull()

            "Installed: ${installTime?.let { dateFormat.format(Date(it)) } ?: "Unknown"}".also { install.text = it }
            "Updated: ${updateTime?.let { dateFormat.format(Date(it)) } ?: "Unknown"}".also { update.text = it }
            "Screen Time: ${formatMillis(screenTime)}".also { screen.text = it }

            dialog.setContentView(view)
            dialog.show()
        }

        @SuppressLint("DefaultLocale")
        private fun formatMillis(ms: Long?): String {
            val totalSeconds = ms?.div(1000)
            val hours = totalSeconds?.div(3600)
            val minutes = (totalSeconds?.rem(3600))?.div(60)
            val seconds = totalSeconds?.rem(60)
            return String.format("%02dh %02dm %02ds", hours, minutes, seconds)
        }
    }
}
