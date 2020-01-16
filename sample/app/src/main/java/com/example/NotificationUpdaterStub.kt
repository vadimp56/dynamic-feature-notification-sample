package com.example

import android.content.Context
import android.widget.Toast

class NotificationUpdaterStub: NotificationUpdater {
    override fun showNotification(context: Context) {
        Toast.makeText(context, "This is stub implementation", Toast.LENGTH_SHORT).show()
    }
}