package com.example.ppapb_broadcastreceivernotification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class NotifReceiver : BroadcastReceiver() {

    // Method yang akan dipanggil saat BroadcastReceiver menerima broadcast
    override fun onReceive(context: Context?, intent: Intent?) {
        // Memastikan bahwa context dan action dari intent tidak null
        if (context != null && intent?.action != null) {
            // Inisialisasi SharedPreferences untuk menyimpan dan mengambil data
            val sharedPreferences = context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Mendapatkan NotificationManager untuk memperbarui notifikasi
            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Mengambil gambar untuk digunakan dalam notifikasi
            val notifImage = BitmapFactory.decodeResource(context.resources, R.drawable.mashanip)

            // Mengambil nilai like dan dislike saat ini dari SharedPreferences
            var likeCount = sharedPreferences.getInt("like_count", 0)
            var dislikeCount = sharedPreferences.getInt("dislike_count", 0)

            // Menangani aksi berdasarkan action yang diterima dari intent
            when (intent.action) {
                "ACTION_LIKE" -> {
                    // Jika tombol "Like" diklik, increment nilai like
                    likeCount++
                    editor.putInt("like_count", likeCount) // Simpan perubahan ke SharedPreferences
                }
                "ACTION_DISLIKE" -> {
                    // Jika tombol "Dislike" diklik, increment nilai dislike
                    dislikeCount++
                    editor.putInt("dislike_count", dislikeCount) // Simpan perubahan ke SharedPreferences
                }
            }
            editor.apply() // Terapkan perubahan ke SharedPreferences

            // Kirim broadcast untuk memperbarui UI di MainActivity
            val updateUiIntent = Intent("UPDATE_UI")
            context.sendBroadcast(updateUiIntent)

            // Membuat RemoteViews untuk memperbarui tampilan notifikasi
            val remoteViews = RemoteViews(context.packageName, R.layout.notification_layout).apply {
                // Perbarui teks jumlah like dan dislike di notifikasi
                setTextViewText(R.id.like_counter, likeCount.toString())
                setTextViewText(R.id.dislike_counter, dislikeCount.toString())
                setImageViewBitmap(R.id.notif_image, notifImage) // Set gambar notifikasi

                // Membuat PendingIntent untuk tombol Like dan Dislike
                val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                // Intent untuk tombol "Like"
                val likeIntent = Intent(context, NotifReceiver::class.java).apply {
                    action = "ACTION_LIKE"
                }
                // Intent untuk tombol "Dislike"
                val dislikeIntent = Intent(context, NotifReceiver::class.java).apply {
                    action = "ACTION_DISLIKE"
                }

                // Pasang PendingIntent pada tombol di notifikasi
                setOnClickPendingIntent(
                    R.id.btn_like,
                    PendingIntent.getBroadcast(context, 0, likeIntent, flag)
                )
                setOnClickPendingIntent(
                    R.id.btn_dislike,
                    PendingIntent.getBroadcast(context, 1, dislikeIntent, flag)
                )
            }

            // Builder untuk membangun ulang notifikasi dengan pembaruan nilai
            val builder = NotificationCompat.Builder(context, "TEST_NOTIF")
                .setSmallIcon(R.drawable.mpreg) // Icon kecil untuk notifikasi
                .setCustomContentView(remoteViews) // Tampilan kecil
                .setCustomBigContentView(remoteViews) // Tampilan besar
                .setAutoCancel(true) // Tutup notifikasi saat diklik

            // Perbarui notifikasi dengan ID yang sama agar data diperbarui
            notifManager.notify(90, builder.build())
        }
    }
}
