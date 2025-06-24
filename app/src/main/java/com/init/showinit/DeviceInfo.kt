package com.init.showinit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    val name: String = "",
    val packageName: String = "",
    val iconBase64: String = "",
    val versionName: String = ""
) : Parcelable

@Parcelize
data class DeviceInfo(
    val deviceID: String = "",
    val deviceName: String = "",
    val battery: Int = -1,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val networkType: String = "",
    val appCount: Int = 0,
    val apps: List<AppInfo> = emptyList(),

    // Added nullable properties for extended info:
    val deviceModel: String? = null,
    val manufacturer: String? = null,
    val androidVersion: String? = null
) : Parcelable
