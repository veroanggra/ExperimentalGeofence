package com.veroanggra.experimentalgeofence

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.random.Random

class CampaignService: JobService() {
    var jobCancelled = false

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job started")
        doBackgroundWork(params)
        return true
    }

    private fun doBackgroundWork(params: JobParameters?) {
        Thread {
            kotlin.run {
                if (jobCancelled) {
                    return@Thread
                }
                showNotification(applicationContext, "Reminder job service scheduler")
                jobFinished(params, true)
            }
        }.start()
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job cancelled before completion")
        jobCancelled = true
        return true
    }

    @SuppressLint("ObsoleteSdkInt")
    fun showNotification(context: Context?, message: String) {
        val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
        var notificationId = 1589
        notificationId += Random(notificationId).nextInt(1, 30)

        val notificationBuilder = NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.droid)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.app_name)
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    companion object {
        private val TAG = CampaignService::class.java.simpleName
    }
}