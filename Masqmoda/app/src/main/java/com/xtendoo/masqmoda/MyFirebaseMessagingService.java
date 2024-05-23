package com.xtendoo.masqmoda;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String imageUrl = remoteMessage.getData().get("image");
            String redirectUrl = remoteMessage.getData().get("redirect_url");
            Log.d("MyFirebaseMessaging", "Received message: " + title + ", " + body + ", " + imageUrl + ", " + redirectUrl);

            // Si la aplicación está en primer plano, intenta obtener el título y el cuerpo del objeto RemoteMessage
            if (isAppInForeground(getApplicationContext())) {
                if (remoteMessage.getNotification() != null) {
                    title = remoteMessage.getNotification().getTitle();
                    body = remoteMessage.getNotification().getBody();
                }
            }

            Log.d("MyFirebaseMessaging", "Received message: " + title + ", " + body + ", " + imageUrl + ", " + redirectUrl);

            if (isAppInForeground(getApplicationContext())) {
                // Enviar un broadcast local
                Intent intent = new Intent("MyFirebaseMessagingServiceMessage");
                intent.putExtra("title", title);
                intent.putExtra("body", body);
                intent.putExtra("image", imageUrl);
                intent.putExtra("redirect_url", redirectUrl);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                // Mostrar la notificación del sistema
                sendNotification(title, body, imageUrl, redirectUrl);
            }
        }
    }

    private boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                if (processInfo.processName.equals(context.getPackageName()) && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendNotification(String title, String body, String imageUrl, String redirectUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(redirectUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "default_channel_id";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Si hay una imagen, añádela a la notificación
        if (imageUrl != null && !imageUrl.isEmpty()) {
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.bigPicture(getBitmapFromUrl(imageUrl)).setSummaryText(body);
            notificationBuilder.setStyle(bigPictureStyle);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            InputStream inputStream = new URL(imageUrl).openStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Guarda el nuevo token en Firestore
        TokenManager.guardarTokenEnFirestore(token);

        // Suscribirse al tema "UsuarioApp"
        FirebaseMessaging.getInstance().subscribeToTopic("appUserTopic")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("MyFirebaseMessagingService", "Suscrito al tema appUserTopic");
                        } else {
                            Log.d("MyFirebaseMessagingService", "Error al suscribirse al tema appUserTopic");
                        }
                    }
                });
    }
}