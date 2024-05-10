package com.example.masqmoda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private String initialUrl = "https://masqmoda.net";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // Permite todos los errores SSL
        handler.proceed();
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (error != null && error.getErrorCode() == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            // Aquí puedes manejar el error como desees, por ejemplo, mostrando un Toast
            webView.loadUrl(initialUrl);
            Toast.makeText(MainActivity.this, "Error al intentar cargar la página", Toast.LENGTH_SHORT).show();
        }
    }
}
}

