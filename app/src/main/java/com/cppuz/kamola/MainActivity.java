
package com.cppuz.kamola;

import android.app.*;
import android.content.*;
import android.os.*;
import android.webkit.*;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import org.json.JSONArray;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private static final String CHANNEL_ID = "cppuz_channel";
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable notifRunnable;
    private String[] notifPhrases = new String[]{
        "Kamola, akangizga aytib beraymi dars qilmaganizni? 😾",
        "Kamola, 2 soatdan beri dars qilmayabsiz Opajon! 📚",
        "Kamola, akangiz so'rayapti, bugun dars qildingmi deb? 🤔",
        "Kamola, 3 soat bo'ldi, mushukcha seni kutayapti! 🐱",
        "Kamola, akangizga aytaymi? Darsni tashlab ketding deb? 😿",
        "Kamola, 1 soatdan beri kompilyator yolg'iz qoldi! 💻",
        "Kamola, akangiz hursand bo'lmayapti, dars qilmayapsan! 😾",
        "Kamola, 4 soat bo'ldi, bitta ham qator yozmading! ⏰",
        "Kamola, Opajon, akangiz kelayapti, tez darsga qayt! 🏃",
        "Kamola, mushukcha xafa, akangizga aytib beraymi? 🥺"
    };

    public class AndroidInterface {
        @JavascriptInterface
        public void showNotification(String title, String body){
            showNotif(title, body);
        }
        @JavascriptInterface
        public String getTgToken(){
            return BuildConfig.TG_BOT_TOKEN;
        }
        @JavascriptInterface
        public void scheduleNotifs(String jsonPhrases){
            try{
                JSONArray arr = new JSONArray(jsonPhrases);
                notifPhrases = new String[arr.length()];
                for(int i=0;i<arr.length();i++) notifPhrases[i]=arr.getString(i);
            }catch(Exception e){}
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createChannel();
        if(Build.VERSION.SDK_INT >= 33 &&
           checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        String dbPath = getApplicationContext().getDir("database", MODE_PRIVATE).getPath();
        s.setDatabasePath(dbPath);

        webView.addJavascriptInterface(new AndroidInterface(), "AndroidInterface");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);

        // Har 2 soatda notification - vaqtga qarab
        notifRunnable = new Runnable(){
            @Override
            public void run(){
                SharedPreferences prefs = getSharedPreferences("cppuz", MODE_PRIVATE);
                long last = prefs.getLong("lastActive", System.currentTimeMillis());
                long idle = System.currentTimeMillis() - last;
                if(idle > 3600000){ // 1 soatdan beri yo'q
                    long hours = idle / 3600000;
                    String timeText = hours>=2 ? hours + " soatdan beri dars qilmayabsiz Opajon! " : "";
                    String phrase = notifPhrases[new Random().nextInt(notifPhrases.length)];
                    showNotif("CppUz - Kamola", timeText + phrase);
                }
                handler.postDelayed(this, 7200000); // har 2 soatda
            }
        };
        handler.postDelayed(notifRunnable, 7200000);
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "CppUz Dars", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Dars eslatmalari");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    private void showNotif(String title, String body){
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        b.setContentIntent(pi);
        NotificationManagerCompat.from(this).notify((int)System.currentTimeMillis(), b.build());
    }

    private void markActive(){
        getSharedPreferences("cppuz", MODE_PRIVATE).edit()
            .putLong("lastActive", System.currentTimeMillis()).apply();
    }

    @Override
    protected void onResume(){ super.onResume(); markActive(); }

    @Override
    protected void onPause(){ markActive(); super.onPause(); }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        handler.removeCallbacks(notifRunnable);
        super.onDestroy();
    }
}
