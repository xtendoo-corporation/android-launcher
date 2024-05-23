package com.xtendoo.masqmoda;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String COLLECTION_NAME = "tokens"; // Nombre de la colección en Firestore

    // Método para guardar el token en Firestore
    public static void guardarTokenEnFirestore(String token) {
        // Obtén una instancia de la base de datos Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Crea una referencia al documento en la colección "tokens" usando un ID específico
        DocumentReference docRef = db.collection(COLLECTION_NAME).document("8J501xhrBPn972nbUkSw");

        // Recupera la lista actual de tokens del documento
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Obtiene la lista actual de tokens del documento
                    List<String> tokens = (List<String>) documentSnapshot.get("tokens");

                    // Si la lista es nula o vacía, crea una nueva lista
                    if (tokens == null) {
                        tokens = new ArrayList<>();
                    }

                    // Agrega el nuevo token a la lista
                    tokens.add(token);

                    // Crea un mapa con el campo "tokens" y la lista actualizada
                    Map<String, Object> tokenData = new HashMap<>();
                    tokenData.put("tokens", tokens);

                    // Actualiza el documento en Firestore con la lista actualizada de tokens
                    docRef.set(tokenData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Token guardado correctamente en Firestore: " + token);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error al guardar el token en Firestore", e);
                                }
                            });
                } else {
                    Log.d(TAG, "El documento no existe");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error al obtener el documento", e);
            }
        });
    }
}