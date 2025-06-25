package com.init.showinit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AppInfo(
    var name: String = "",
    var packageName: String = "",
    var iconBase64: String = "",
    var versionName: String = "",
    var category: String = "Other"
)

data class DeviceInfo(
    val deviceID: String = "",
    val deviceName: String = "",
    val battery: Int = -1,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val networkType: String = "",
    val appCount: Int = 0,
    val apps: List<AppInfo> = emptyList(),
    val deviceModel: String? = null,
    val manufacturer: String? = null,
    val androidVersion: String? = null
)
