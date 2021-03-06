package com.kakao.smartmemo.Receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.kakao.smartmemo.R
import com.kakao.smartmemo.View.MainActivity

class TodoReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager: NotificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder  = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent) // 알림을 눌렀을때 실행할 작업 인텐트 설정
            .setWhen(System.currentTimeMillis()) //miliSecond단위로 넣어주면 내부적으로 파싱함.
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setFullScreenIntent(pendingIntent,true) //헤드업알림

        //헤드업알림
        val contentView = RemoteViews(context.packageName, R.layout.todolist_headup_alarm)
        contentView.setTextViewText(R.id.notification_Title, "T O D O L I S T")
        contentView.setTextViewText(R.id.todolist_textView, "1. 영양제 먹기\n2. 학교 가기\n3. 병원가기\n4. 비타민 사기\n5. 카페")
        contentView.setTextViewText(R.id.todolist_location, "● 삼성약국/녹십자약국/온누리약국")
        notificationBuilder.setContent(contentView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Oreo 버전 이후부터 channel설정해줘야함.
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = CHANNEL_DESCRITION }

            notificationManager.createNotificationChannel(serviceChannel)
        }

        notificationManager?.notify(9999, notificationBuilder.build())
    }

    companion object {
        const val CHANNEL_ID = "TODO"
        const val CHANNEL_NAME = "알림채널 TODO"
        const val CHANNEL_DESCRITION = "알림채널 TODO 리시버"
    }
}