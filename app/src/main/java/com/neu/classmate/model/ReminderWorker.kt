package com.neu.classmate.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.neu.classmate.R
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    override fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        val db = FirebaseFirestore.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val targetDate = dateFormat.format(tomorrow.time)

        //Check REMINDERS
        db.collection("users").document(userId).collection("reminders")
            .whereEqualTo("date", targetDate)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    showNotification(
                        title = "Reminder",
                        message = "You have a reminder due tomorrow."
                    )
                }
            }

        //Check TASKS
        db.collection("users").document(userId).collection("tasks")
            .whereEqualTo("dueDate", targetDate)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    showNotification(
                        title = "Task Due",
                        message = "You have a task due tomorrow."
                    )
                }
            }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "reminder_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val id = title.hashCode() // ensures Reminder & Task get different IDs

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }
}