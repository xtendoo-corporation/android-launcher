package com.xtendoo.masqmoda;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private String initialUrl = "https://masqmoda.net";
    private static final int PERMISSION_REQUEST_CODE = 123;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(initialUrl);

    }

    @Override
    public void onBackPressed() {
        // Verifica si el WebView puede retroceder en la historia
        if (webView.canGoBack()) {
            webView.goBack(); // Si es posible, retrocede una página en la historia del WebView
        } else {
            // Si la URL actual es https://masqmoda.net, no hagas nada
            if (!webView.getUrl().equals(initialUrl)) {
                super.onBackPressed(); // Si no es posible retroceder en la historia, deja que la actividad maneje el comportamiento predeterminado del botón de retroceso
            }
        }
    }

public class MyWebViewClient extends WebViewClient {

    ArrayList<String> direcciones = new ArrayList<>();
    private MainActivity activity; // MainActivity es tu actividad que contiene el WebView

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // Permite todos los errores SSL
        handler.proceed();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
//        if (errorCode == WebViewClient.ERROR_BAD_URL) {
            // Construir el cuadro de diálogo
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Error de conexión");
            builder.setMessage("Necesitas conexión a internet para acceder.");
            builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Cerrar la aplicación
                    finish();
                }
            });
            builder.setCancelable(false); // Evitar que el usuario pueda cerrar el cuadro de diálogo presionando fuera de él

            // Mostrar el cuadro de diálogo
            AlertDialog dialog = builder.create();
            dialog.show();
//        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (direcciones.isEmpty()){
            // Agregar elementos al ArrayList
            direcciones.add("instagram.com");
            direcciones.add("facebook.com");
            direcciones.add("tiktok.com");
            direcciones.add("stripe.com");
            direcciones.add("checkout.link.com");
            direcciones.add("tel:");
            direcciones.add("mailto:");

        }
        Uri url = request.getUrl();
        for (int x=0 ; x < direcciones.size(); x++){
            if (url.toString().contains(direcciones.get(x))){
                if (url.toString().contains("tel:")){
                    Intent intent = new Intent(Intent.ACTION_DIAL, url);
                    view.getContext().startActivity(intent);
                }
                if (url.toString().contains("mailto:")){
                    Intent intent = new Intent(Intent.ACTION_SENDTO, url);
                    view.getContext().startActivity(intent);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    view.getContext().startActivity(intent);
                }
                return true; // Indicamos que la navegación debe ser manejada por el WebView
            }
        }
        return false; // Dejamos que el WebView maneje la navegación
    }
}
}

