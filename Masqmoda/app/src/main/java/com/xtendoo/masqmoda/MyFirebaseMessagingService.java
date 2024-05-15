package com.xtendoo.masqmoda;

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
    }
}
