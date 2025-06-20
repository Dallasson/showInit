package com.init.showinit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceInfo(
    val deviceID: String = "",
    val deviceName: String = "",
    val battery: Int = -1,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val networkType: String = "",
    val appCount: Int = 0, // still used for Firebase fallback or debugging
    val apps: List<AppInfo> = emptyList()
) : Parcelable

@Parcelize
data class AppInfo(
    val name: String = "",
    val packageName: String = "",
    val iconBase64: String = ""
) : Parcelable