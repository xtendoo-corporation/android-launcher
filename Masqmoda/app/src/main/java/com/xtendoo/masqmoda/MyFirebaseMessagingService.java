package com.xtendoo.masqmoda;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Procesar la notificación recibida
        // remoteMessage contiene información sobre la notificación
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
