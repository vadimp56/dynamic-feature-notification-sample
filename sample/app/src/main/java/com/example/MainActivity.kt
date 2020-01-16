package com.example

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.android.play.core.splitinstall.testing.FakeSplitInstallManager
import com.google.android.play.core.splitinstall.testing.FakeSplitInstallManagerFactory
import java.io.File

class MainActivity : AppCompatActivity() {
    private var splitInstallStateUpdatedListener: SplitInstallStateUpdatedListener? = null
    private var fakeSplitInstallManager: FakeSplitInstallManager? = null
    private lateinit var featureSplitsPathEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerNotificationChannel()
        }
        featureSplitsPathEditText = findViewById<EditText>(R.id.feature_module_path)
        val mainNotificationButton = findViewById<Button>(R.id.main_notification_button)
        mainNotificationButton.setOnClickListener { showNotification() }
        val featureNotificationButton = findViewById<Button>(R.id.feature_notification)
        featureNotificationButton.setOnClickListener { showFeatureNotification() }
    }

    override fun onStop() {
        super.onStop()
        if (fakeSplitInstallManager != null && splitInstallStateUpdatedListener != null) {
            fakeSplitInstallManager!!.unregisterListener(splitInstallStateUpdatedListener)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun registerNotificationChannel() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT)
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this,
            NOTIFICATION_CHANNEL_ID
        )
        val notification = builder
            .setContentTitle("Main notification")
            .setSmallIcon(R.drawable.main_icon)
            .build()
        NotificationManagerCompat.from(this).notify(1, notification)
    }

    private fun showFeatureNotification() {
        val splitInstallManagerFactory = SplitInstallManagerFactory.create(this)
        if (splitInstallManagerFactory.installedModules.contains(NOTIFICATIONS_MODULE_NAME)) {
            createNotificationUpdater().showNotification(this)
        } else {
            fakeSplitInstallManager = createFakeSplitInstallManager()
            val request = SplitInstallRequest.newBuilder()
                .addModule(NOTIFICATIONS_MODULE_NAME)
                .build()
            splitInstallStateUpdatedListener = createSplitInstallStateUpdatedListener()
            fakeSplitInstallManager?.registerListener(splitInstallStateUpdatedListener)
            fakeSplitInstallManager?.startInstall(request)
                ?.addOnFailureListener { Toast.makeText(this, "Installing fail", Toast.LENGTH_SHORT).show() }
                ?.addOnSuccessListener {
                    SplitCompat.install(applicationContext)
                    Toast.makeText(
                        this,
                        "Installing with sessionId = $it",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun createSplitInstallStateUpdatedListener(): SplitInstallStateUpdatedListener {
        return object : SplitInstallStateUpdatedListener {
            override fun onStateUpdate(state: SplitInstallSessionState?) {
                when(state?.status()) {
                    SplitInstallSessionStatus.INSTALLED -> {
                        Toast.makeText(this@MainActivity, "Feature installed", Toast.LENGTH_SHORT).show()
                        fakeSplitInstallManager?.unregisterListener(this)
                        createNotificationUpdater().showNotification(this@MainActivity)
                    }
                    SplitInstallSessionStatus.FAILED -> {
                        Toast.makeText(this@MainActivity, "Feature installing failed", Toast.LENGTH_SHORT).show()
                        fakeSplitInstallManager?.unregisterListener(this)
                    }
                }
            }
        }
    }

    private fun createFakeSplitInstallManager() = FakeSplitInstallManagerFactory.create(
        this,
        File(featureSplitsPathEditText.text.toString())
    )

    private fun createNotificationUpdater(): NotificationUpdater {
        var notificationUpdater: NotificationUpdater
        try {
            notificationUpdater =
                Class.forName("com.example.dynamic.notifications.NotificationUpdaterImpl").newInstance()
                    as NotificationUpdater
        } catch (ex: Throwable) {
            notificationUpdater =
                NotificationUpdaterStub()
        }
        return notificationUpdater
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notification_channel"
        const val NOTIFICATIONS_MODULE_NAME = "notifications"
    }
}
