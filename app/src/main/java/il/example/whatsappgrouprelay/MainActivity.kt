package il.example.whatsappgrouprelay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var sourceInput: EditText
    private lateinit var targetInput: EditText
    private lateinit var status: TextView
    private lateinit var messages: TextView
    private val prefs by lazy { getSharedPreferences("relay", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refreshMessages()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
        val scroller = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        content.addView(TextView(this).apply {
            text = "מעביר הודעות WhatsApp"
            textSize = 24f
            gravity = Gravity.CENTER
        })
        content.addView(TextView(this).apply {
            text = "אב־טיפוס: מזהה התראות ושומר הודעות בפורמט קבוצה + שולח + תוכן. הוא עדיין לא שולח אוטומטית לקבוצת היעד."
            textSize = 15f
            setPadding(0, 16, 0, 16)
        })

        sourceInput = EditText(this).apply {
            hint = "שם קבוצת המקור (כפי שמופיע בהתראה)"
            setText(prefs.getString("source", ""))
        }
        targetInput = EditText(this).apply {
            hint = "שם קבוצת היעד (לתיעוד בלבד)"
            setText(prefs.getString("target", ""))
        }
        content.addView(sourceInput)
        content.addView(targetInput)

        content.addView(Button(this).apply {
            text = "שמור הגדרות"
            setOnClickListener {
                prefs.edit()
                    .putString("source", sourceInput.text.toString().trim())
                    .putString("target", targetInput.text.toString().trim())
                    .apply()
                Toast.makeText(this@MainActivity, "ההגדרות נשמרו", Toast.LENGTH_SHORT).show()
            }
        })

        content.addView(Button(this).apply {
            text = "הפעל הרשאת גישה להתראות"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        })

        status = TextView(this).apply {
            textSize = 14f
            setPadding(0, 12, 0, 12)
        }
        content.addView(status)

        content.addView(Button(this).apply {
            text = "רענן הודעות שנקלטו"
            setOnClickListener { refreshMessages() }
        })
        content.addView(Button(this).apply {
            text = "שתף את ההודעה האחרונה"
            setOnClickListener { shareLatest() }
        })
        messages = TextView(this).apply {
            textSize = 14f
            setPadding(0, 12, 0, 24)
        }
        content.addView(messages)
        scroller.addView(content)
        root.addView(scroller)
        setContentView(root)
    }

    private fun refreshMessages() {
        val all = getSharedPreferences("captured", MODE_PRIVATE)
            .getString("items", "") ?: ""
        status.text = if (WhatsAppNotificationListener.connected)
            "שירות ההתראות מחובר כרגע."
        else
            "אם לא נקלטות הודעות, ודא שהרשאת ההתראות הופעלה."
        messages.text = if (all.isBlank()) "עדיין לא נקלטו הודעות." else all
    }

    private fun shareLatest() {
        val all = getSharedPreferences("captured", MODE_PRIVATE)
            .getString("items", "") ?: ""
        val latest = all.lineSequence().lastOrNull { it.isNotBlank() }
        if (latest == null) {
            Toast.makeText(this, "אין עדיין הודעות לשיתוף", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, latest)
        }
        startActivity(Intent.createChooser(intent, "שתף הודעה"))
    }
}
