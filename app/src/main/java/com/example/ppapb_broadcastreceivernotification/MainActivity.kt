package com.example.ppapb_broadcastreceivernotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.ppapb_broadcastreceivernotification.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    // BroadcastReceiver untuk memperbarui UI saat ada perubahan di SharedPreferences
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "UPDATE_UI") {
                // Ambil nilai terbaru dari SharedPreferences
                val likeCount = sharedPreferences.getInt("like_count", 0)
                val dislikeCount = sharedPreferences.getInt("dislike_count", 0)

                // Perbarui tampilan TextView dengan nilai terbaru
                binding.tvLikeCounter.text = likeCount.toString()
                binding.tvDislikeCounter.text = dislikeCount.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan View Binding untuk menghubungkan layout dengan aktivitas
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SharedPreferences untuk menyimpan data persist
        sharedPreferences = getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)

        // Daftarkan BroadcastReceiver untuk mendengarkan perubahan data
        val intentFilter = IntentFilter("UPDATE_UI")
        registerReceiver(broadcastReceiver, intentFilter)

        // Set nilai awal pada TextView untuk counter "Like" dan "Dislike"
        binding.tvLikeCounter.text = sharedPreferences.getInt("like_count", 0).toString()
        binding.tvDislikeCounter.text = sharedPreferences.getInt("dislike_count", 0).toString()

        // Listener untuk tombol notifikasi
        binding.btnNotif.setOnClickListener {
            // Menampilkan notifikasi dengan RemoteViews (custom layout)
            val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notifImage = BitmapFactory.decodeResource(resources, R.drawable.mashanip)

            // RemoteViews digunakan untuk mendesain notifikasi kustom
            val remoteViews = RemoteViews(packageName, R.layout.notification_layout).apply {
                // Inisialisasi elemen pada layout notifikasi
                setTextViewText(R.id.like_counter, "0")
                setTextViewText(R.id.dislike_counter, "0")
                setImageViewBitmap(R.id.notif_image, notifImage)

                // Konfigurasi PendingIntent untuk tombol "Like" dan "Dislike"
                val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                // Intent untuk tombol "Like"
                val likeIntent = Intent(this@MainActivity, NotifReceiver::class.java).apply {
                    action = "ACTION_LIKE"
                }

                // Intent untuk tombol "Dislike"
                val dislikeIntent = Intent(this@MainActivity, NotifReceiver::class.java).apply {
                    action = "ACTION_DISLIKE"
                }

                // Hubungkan tombol di notifikasi dengan PendingIntent
                setOnClickPendingIntent(
                    R.id.btn_like,
                    PendingIntent.getBroadcast(this@MainActivity, 0, likeIntent, flag)
                )
                setOnClickPendingIntent(
                    R.id.btn_dislike,
                    PendingIntent.getBroadcast(this@MainActivity, 1, dislikeIntent, flag)
                )
            }

            // Membuat NotificationChannel untuk Android Oreo (API 26+) ke atas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "TEST_NOTIF", // ID channel
                    "Test Notification", // Nama channel
                    NotificationManager.IMPORTANCE_DEFAULT // Level pentingnya notifikasi
                )
                notifManager.createNotificationChannel(channel)
            }

            // Membuat builder untuk notifikasi
            val builder = NotificationCompat.Builder(this@MainActivity, "TEST_NOTIF")
                .setSmallIcon(R.drawable.mpreg) // Ikon notifikasi kecil
                .setCustomContentView(remoteViews) // Layout kustom untuk notifikasi
                .setCustomBigContentView(remoteViews) // Layout kustom saat diperbesar
                .setAutoCancel(true) // Menutup notifikasi setelah diklik

            // Menampilkan notifikasi dengan ID 90
            notifManager.notify(90, builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister BroadcastReceiver saat Activity dihancurkan
        unregisterReceiver(broadcastReceiver)
    }
}


