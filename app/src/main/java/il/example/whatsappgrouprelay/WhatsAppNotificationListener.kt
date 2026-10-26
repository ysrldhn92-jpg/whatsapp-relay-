package il.example.whatsappgrouprelay

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils

class WhatsAppNotificationListener : NotificationListenerService() {
    companion object {
        @Volatile var connected: Boolean = false
    }

    override fun onListenerConnected() {
        connected = true
    }

    override fun onListenerDisconnected() {
        connected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.whatsapp" &&
            sbn.packageName != "com.whatsapp.w4b") return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val cleanText = text.trim()
        if (title.isBlank() || cleanText.isBlank()) return

        val prefs = getSharedPreferences("relay", MODE_PRIVATE)
        val source = prefs.getString("source", "").orEmpty().trim()
        if (source.isNotBlank() && !title.contains(source, ignoreCase = true)) return

        // WhatsApp notification layouts vary by version and notification privacy settings.
        // The notification text may already include a sender name, or may be grouped/hidden.
        val formatted = "קבוצה: $title | הודעה: $cleanText"
        val store = getSharedPreferences("captured", MODE_PRIVATE)
        val previous = store.getString("items", "").orEmpty()
        val updated = (previous.lines() + formatted)
            .filter { it.isNotBlank() }
            .takeLast(30)
            .joinToString("\n")
        store.edit().putString("items", updated).apply()
    }
}
