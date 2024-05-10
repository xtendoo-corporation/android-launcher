package com.example.masqmoda;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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


}

