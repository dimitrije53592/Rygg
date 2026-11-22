package com.dimitrijeilic.rygg.feature.offline.data

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun freeBytes(): Long = StatFs(context.filesDir.path).availableBytes
}
