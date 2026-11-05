package com.dimitrijeilic.rygg.feature.offline.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.dimitrijeilic.rygg.R
import com.dimitrijeilic.rygg.core.notification.NotificationHelper
import com.dimitrijeilic.rygg.core.notification.OFFLINE_DOWNLOAD_NOTIFICATION_ID
import com.dimitrijeilic.rygg.core.notification.OFFLINE_READY_NOTIFICATION_ID
import com.dimitrijeilic.rygg.core.notification.RyggChannel
import com.dimitrijeilic.rygg.core.notification.SimpleNotificationBuilder
import com.dimitrijeilic.rygg.core.units.UnitFormatter
import com.dimitrijeilic.rygg.feature.offline.data.OfflineRegionRepository
import com.dimitrijeilic.rygg.feature.offline.domain.OfflineRegion
import com.dimitrijeilic.rygg.feature.offline.domain.RegionStatus
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OfflineDownloadService : Service() {
    @Inject
    lateinit var repository: OfflineRegionRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var watchJob: Job? = null
    private var regionId: Long? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundDownload()
                val id = intent.getLongExtra(EXTRA_REGION_ID, NO_REGION)
                if (id == NO_REGION) stopDownload() else watch(id)
            }

            ACTION_PAUSE -> serviceScope.launch {
                regionId?.let { repository.pause(it) }
                stopDownload()
            }
        }
        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        serviceScope.launch {
            regionId?.let { repository.pause(it) }
            stopDownload()
        }
    }

    private fun watch(id: Long) {
        regionId = id
        watchJob?.cancel()
        watchJob = serviceScope.launch {
            val units = UnitFormatter(resources, settingsRepository.currentMeasurementSystem())
            var hasStarted = false
            var shownPercent = -1
            repository.observeRegions().collect { regions ->
                val region = regions.firstOrNull { it.id == id }
                when (region?.status) {
                    null, RegionStatus.Failed -> stopDownload()
                    RegionStatus.Complete -> {
                        notifyReady(region)
                        stopDownload()
                    }
                    RegionStatus.Paused -> if (hasStarted) stopDownload()
                    RegionStatus.Downloading -> {
                        hasStarted = true
                        val percent = (region.progress * PERCENT).toInt()
                        if (percent == shownPercent) return@collect
                        shownPercent = percent
                        notificationHelper.notify(
                            builder = progressNotification(region, units.fileSize(region.sizeBytes)),
                            notificationId = OFFLINE_DOWNLOAD_NOTIFICATION_ID,
                            channel = RyggChannel.DOWNLOADS
                        )
                    }
                }
            }
        }
    }

    private fun notifyReady(region: OfflineRegion) {
        val name = region.name.ifBlank { getString(R.string.offline_unnamed) }
        notificationHelper.notify(
            builder = SimpleNotificationBuilder(
                title = getString(R.string.offline_ready_title),
                message = getString(R.string.offline_ready_message, name)
            ),
            notificationId = OFFLINE_READY_NOTIFICATION_ID
        )
    }

    private fun startForegroundDownload() {
        val notification = notificationHelper.build(
            builder = progressNotification(region = null, sizeText = ""),
            channel = RyggChannel.DOWNLOADS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                OFFLINE_DOWNLOAD_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ServiceCompat.startForeground(this, OFFLINE_DOWNLOAD_NOTIFICATION_ID, notification, 0)
        }
    }

    private fun stopDownload() {
        watchJob?.cancel()
        regionId = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun progressNotification(region: OfflineRegion?, sizeText: String) =
        OfflineDownloadNotificationBuilder(
            regionName = region?.name?.ifBlank { null } ?: getString(R.string.offline_unnamed),
            progress = region?.progress ?: 0f,
            sizeText = sizeText,
            contentPendingIntent = contentPendingIntent(),
            pausePendingIntent = servicePendingIntent(ACTION_PAUSE, REQUEST_CODE_PAUSE)
        )

    private fun contentPendingIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            REQUEST_CODE_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, OfflineDownloadService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.dimitrijeilic.rygg.offline.START"
        const val ACTION_PAUSE = "com.dimitrijeilic.rygg.offline.PAUSE"

        private const val EXTRA_REGION_ID = "region_id"
        private const val NO_REGION = -1L
        private const val REQUEST_CODE_PAUSE = 300
        private const val REQUEST_CODE_CONTENT = 301
        private const val PERCENT = 100

        fun start(context: Context, regionId: Long) {
            val intent = Intent(context, OfflineDownloadService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_REGION_ID, regionId)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
