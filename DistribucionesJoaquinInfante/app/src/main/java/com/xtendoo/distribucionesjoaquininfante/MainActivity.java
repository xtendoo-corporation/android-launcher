package com.xtendoo.distribucionesjoaquininfante;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
//
    private String initialUrl = "https://distribucionesjoaquininfante.es/";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private BroadcastReceiver mMessageReceiver;
//    private static final String TAG = "SSL_ERROR";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar si los permisos de notificación están otorgados
        if (!checkNotificationPermissions()) {
            // Si los permisos no están otorgados, mostrar el diálogo de solicitud de permisos
            requestNotificationPermissions();
        }

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
                    }
                });

        webView = findViewById(R.id.webView);
        configureWebView();
//        webView.setWebViewClient(new MyWebViewClient());
//        webView.getSettings().setJavaScriptEnabled(true);
//        String redirectUrl = getIntent().getStringExtra("redirect_url");
//        if (redirectUrl != null && !redirectUrl.isEmpty()) {
//            webView.loadUrl(redirectUrl);
//        } else {
//            webView.loadUrl(initialUrl);
//        }

        // Inicializar el BroadcastReceiver
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String title = intent.getStringExtra("title");
                String body = intent.getStringExtra("body");
                String imageUrl = intent.getStringExtra("image");
                String redirectUrl = intent.getStringExtra("redirect_url");

                Log.d("MainActivity", "Received message: " + title + ", " + body + ", " + imageUrl + ", " + redirectUrl);

                // Mostrar la notificación dentro de la aplicación como Snackbar
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), title + ": " + body, Snackbar.LENGTH_LONG)
                        .setAction("Ver", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                                    webView.loadUrl(redirectUrl);
                                }
                            }
                        });
                // Puedes cambiar la duración en milisegundos
                int duracionPersonalizada = 7500; // 10 segundos
                snackbar.setDuration(duracionPersonalizada);
                snackbar.show();
            }
        };
    }

    private void configureWebView() {
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(false); // Desactiva JavaScript si no es necesario

        // Configura otras opciones de seguridad
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setDomStorageEnabled(false);

        String redirectUrl = getIntent().getStringExtra("redirect_url");
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            loadUrlSafely(redirectUrl);
        } else {
            // Carga una URL de forma segura
            loadUrlSafely(initialUrl);
        }
    }

    private void loadUrlSafely(String url) {
        // Sanitiza la URL antes de cargarla
        if (url != null && (url.startsWith("https://") || url.startsWith("http://"))) {
            webView.loadUrl(url);
        } else {
            // Maneja el caso de una URL no válida
            // Por ejemplo, muestra un mensaje de error al usuario
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("MyFirebaseMessagingServiceMessage"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Obtener la URL de redirección del nuevo Intent
        String redirectUrl = intent.getStringExtra("redirect_url");
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            loadUrlSafely(redirectUrl);
        }
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

//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            // Permite todos los errores SSL
//            handler.proceed();
//        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // Registrar el error SSL
            logSslError(error);

            // Muestra un diálogo de advertencia al usuario
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("Hay un problema con el certificado de seguridad del sitio. ¿Deseas continuar de todos modos?");

            builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Permitir que el usuario decida continuar
                    handler.proceed();
                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Cancelar la carga de la página
                    handler.cancel();
                }
            });

            builder.create().show();
        }

        private void logSslError(SslError error) {
            String errorMessage;
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    errorMessage = "El certificado no es de confianza.";
                    break;
                case SslError.SSL_EXPIRED:
                    errorMessage = "El certificado ha expirado.";
                    break;
                case SslError.SSL_IDMISMATCH:
                    errorMessage = "El nombre del certificado no coincide.";
                    break;
                case SslError.SSL_NOTYETVALID:
                    errorMessage = "El certificado aún no es válido.";
                    break;
                default:
                    errorMessage = "Error SSL desconocido.";
                    break;
            }
            Log.e(TAG, "Error SSL: " + errorMessage);
            Log.e(TAG, "URL: " + error.getUrl());

            // Obtener información del certificado
            SslCertificate sslCertificate = error.getCertificate();
            if (sslCertificate != null) {
                SslCertificate.DName issuedTo = sslCertificate.getIssuedTo();
                SslCertificate.DName issuedBy = sslCertificate.getIssuedBy();
                Log.e(TAG, "Issued to: " + issuedTo.getDName());
                Log.e(TAG, "Issued by: " + issuedBy.getDName());
            }
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

    //Permisos de notificaciones
    // Método para verificar si los permisos de notificación están otorgados
    private boolean checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        } else {
            // No es posible verificar si los permisos están otorgados en versiones anteriores a Oreo
            return true;
        }
    }

    // Método para solicitar permisos de notificación
    private void requestNotificationPermissions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permisos de notificación")
                .setMessage("Esta aplicación requiere permisos de notificación para funcionar correctamente. " +
                        "Por favor, habilite los permisos en la configuración de la aplicación.")
                .setPositiveButton("Ir a configuración", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ir a la configuración de la aplicación para habilitar los permisos de notificación
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null)));
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // El usuario canceló la solicitud de permisos, puedes manejarlo según sea necesario
                        Toast.makeText(MainActivity.this, "Los permisos de notificación no están habilitados. La aplicación puede no funcionar correctamente.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // Método para manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            // Verificar si el usuario otorgó los permisos de notificación
            if (checkNotificationPermissions()) {
                // Permiso concedido, puedes continuar con la lógica de tu aplicación
                Toast.makeText(this, "Permisos de notificación concedidos.", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado, puedes mostrar un mensaje al usuario informándole sobre la importancia de los permisos
                Toast.makeText(this, "Los permisos de notificación no están habilitados. La aplicación puede no funcionar correctamente.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
