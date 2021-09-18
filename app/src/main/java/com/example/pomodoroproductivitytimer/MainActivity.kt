package com.example.pomodoroproductivitytimer

import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    val colorGenerator: () -> Int = colorGeneratorFactory()

    var limit = 0

    private val textView: TextView by lazy {
        findViewById<TextView>(R.id.textView)
    }
    private val progressBar: ProgressBar by lazy {
        findViewById<ProgressBar>(R.id.progressBar)
    }
    private val settingsButton: Button by lazy {
        findViewById<Button>(R.id.settingsButton)
    }

    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun startButtonClicked(view: View) {
        if (timer == null) {
            progressBar.visibility = View.VISIBLE
            settingsButton.isEnabled = false
            timer = Timer()
            timer?.scheduleAtFixedRate(createdNewTimerTask(), 0L, 1000L)
        }

    }

    fun resetButtonClicked(view: View) {
        progressBar.visibility = View.INVISIBLE
        settingsButton.isEnabled = true
        textView.text = "00:00"
        textView.setTextColor(Color.BLACK)
        timer?.cancel()
        timer = null
    }

    fun settingsButtonClicked(view: View) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_time, null)

        dialogBuilder
            .setView(dialogLayout)
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }.setPositiveButton("OK") { dialog, which ->
                val textInput = dialogLayout.findViewById<EditText>(R.id.upperLimitEditText)
                val numberInput = textInput.text.toString().toIntOrNull() ?: 0
                limit = numberInput
                dialog.dismiss()
            }.show()

    }

    private fun createdNewTimerTask(): TimerTask {
        return object : TimerTask() {

            val startTime = System.currentTimeMillis()
            val immutableLimit = limit

            override fun run() {

                val currentTime = System.currentTimeMillis()
                val elapsed = (currentTime - startTime) / 1000
                val seconds = String.format("%02d", elapsed % 60)
                val minutes = String.format("%02d", elapsed / 60)

                runOnUiThread {
                    if (immutableLimit > 0 && elapsed > immutableLimit) {
                        textView.setTextColor(Color.RED)
                        addNotification()
                    }
                    progressBar.indeterminateTintList = ColorStateList.valueOf(colorGenerator())
                    textView.text = "$minutes:$seconds"
                }
            }
        }
    }

    private fun colorGeneratorFactory(): () -> Int {

        var currentIndex = 0
        val colorList = listOf(
            Color.BLUE,
            Color.DKGRAY,
            Color.YELLOW,
            Color.GRAY,
            Color.MAGENTA,
            Color.BLACK,
            Color.GREEN,
            Color.LTGRAY
        )

        return {
            currentIndex = (currentIndex + 1) % colorList.size
            colorList[currentIndex]
        }
    }

    private fun addNotification() {
        val builder = NotificationCompat.Builder(this, "org.hyperskill")
            .setSmallIcon(R.drawable.clock)
            .setContentTitle("Pomodoro Productivity Timer")
            .setContentText("Time exceeded")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(contentIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(393939, builder.build())
    }
}
