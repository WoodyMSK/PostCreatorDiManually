package ru.netology.nmedia.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.Constants.TAG
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.di.DependencyContainer
import kotlin.random.Random


const val channelId = "notification_chanel"
const val channelName = "ru.netology.nmedia.service"

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val recipientId = "recipientId"
    private val gson = Gson()
    private val container by lazy { DependencyContainer.getInstance(application) }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        // Приём push-уведовлеия и вывод Toast

//    val intent = Intent(INTENT_FILTER)
//    message.data.forEach { entity ->
//        intent.putExtra(entity.key, entity.value)
//    }
//
//    sendBroadcast(intent)

//         Приём push-уведовлеия с payload, и его вывод, если "action": "LIKE". Изначально было в коде

        message.data[action]?.let {
           when (Action.valueOf(it)) {
              Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))

           }
        }

        // Для ДЗ

        Log.d("TAG", "From Server")
        Log.d("TAG", (gson.fromJson(message.data[content], PushMessage::class.java).recipientId.toString()))
        Log.d("TAG", "From Local Disc")
        Log.d("TAG", (container.auth.authStateFlow.value.id.toString()))

        if ((gson.fromJson(message.data[content], PushMessage::class.java).recipientId.toString()) == (container.auth.authStateFlow.value.id.toString()) ||
            (gson.fromJson(message.data[content], PushMessage::class.java).recipientId.toString()) == "null") {
            pushMessage(gson.fromJson(message.data[content], PushMessage::class.java))
        } else {
            container.auth.sendPushToken((container.auth.authStateFlow.value.token))
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        container.auth.sendPushToken(token)
    }


    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    // Функция для ДЗ

    private fun pushMessage(message: PushMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("NMedia")
            .setContentText(message.content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(title: String, message: String): RemoteViews {
        val remoteView = RemoteViews("ru.netology.nmedia.service", R.layout.notification)

        remoteView.setTextViewText(R.id.title, title)
        remoteView.setTextViewText(R.id.message, message)
        remoteView.setImageViewResource(R.id.app_logo, R.drawable.ic_netology_48dp)

        return remoteView
    }


    companion object {
        const val INTENT_FILTER = "PUSH_EVENT"
        const val KEY_ACTION = "action"
        const val KEY_MESSAGE = "message"

        const val ACTION_SHOW_MESSAGE = "show_message"
    }
}


enum class Action {
    LIKE,
}

data class PushMessage(
    val recipientId: Long?,
    val content: String,
)

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

