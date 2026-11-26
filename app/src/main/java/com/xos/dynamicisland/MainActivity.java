package com.xos.dynamicisland;

import android.app.*;
import android.os.*;
import android.content.*;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout root;
    android.content.SharedPreferences prefs;
    TextView sizeInfo;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    void updateIsland() {
        sendBroadcast(new Intent("com.xos.dynamicisland.UPDATE"));
    }

    void button(String name, Runnable action) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(Color.rgb(35,35,35))
        );
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, dp(50));
        p.setMargins(0,dp(4),0,dp(4));
        root.addView(b,p);
        b.setOnClickListener(v -> action.run());
    }

    void heading(String name) {
        TextView t = new TextView(this);
        t.setText(name);
        t.setTextColor(Color.WHITE);
        t.setTextSize(18);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0,dp(18),0,dp(10));
        root.addView(t);
    }

    void info(String name) {
        TextView t = new TextView(this);
        t.setText(name);
        t.setTextColor(Color.LTGRAY);
        t.setTextSize(14);
        t.setGravity(Gravity.CENTER);
        root.addView(t);
    }

    void change(String key, int amount, int min, int max) {
        int value = prefs.getInt(key, 0);
        value = Math.max(min, Math.min(max, value + amount));
        prefs.edit().putInt(key,value).apply();
        refresh();
        updateIsland();
    }

    void refresh() {
        int x = prefs.getInt("x",0);
        int y = prefs.getInt("y",dp(5));
        int w = prefs.getInt("width",112);
        int h = prefs.getInt("height",34);

        sizeInfo.setText(
            "X: "+x+"   Y: "+y+
            "\nWidth: "+w+"   Height: "+h
        );
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        prefs = getSharedPreferences("XOS_POSITION",MODE_PRIVATE);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(20),dp(18),dp(25));
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("XOS");
        title.setTextSize(36);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        info("DYNAMIC ISLAND 26 • PRO");

        heading("PERMISSIONS");

        button("ALLOW DISPLAY OVER OTHER APPS", () ->
            startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:"+getPackageName())
            ))
        );

        button("ENABLE NOTIFICATION ACCESS", () ->
            startActivity(new Intent(
                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
            ))
        );

        button("ENABLE USAGE ACCESS", () ->
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        );

        heading("ISLAND POSITION");

        button("▲  UP", () -> change("y",-dp(10),0,3000));
        button("▼  DOWN", () -> change("y",dp(10),0,3000));
        button("◀  LEFT", () -> change("x",-dp(10),-1000,1000));
        button("▶  RIGHT", () -> change("x",dp(10),-1000,1000));

        heading("ISLAND SIZE");

        button("WIDTH  +", () -> change("width",8,90,300));
        button("WIDTH  -", () -> change("width",-8,90,300));
        button("HEIGHT  +", () -> change("height",4,28,100));
        button("HEIGHT  -", () -> change("height",-4,28,100));

        sizeInfo = new TextView(this);
        sizeInfo.setTextColor(Color.WHITE);
        sizeInfo.setTextSize(16);
        sizeInfo.setGravity(Gravity.CENTER);
        sizeInfo.setPadding(0,dp(12),0,dp(12));
        root.addView(sizeInfo);
        refresh();

        button("RESET POSITION & SIZE", () -> {
            prefs.edit()
                .putInt("x",0)
                .putInt("y",dp(5))
                .putInt("width",112)
                .putInt("height",34)
                .apply();
            refresh();
            updateIsland();
        });

        heading("ISLAND CONTROL");

        button("START XOS ISLAND", () -> {
            if (Settings.canDrawOverlays(this)) {
                Intent i = new Intent(this,IslandService.class);
                if (Build.VERSION.SDK_INT >= 26)
                    startForegroundService(i);
                else startService(i);
            } else {
                startActivity(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:"+getPackageName())
                ));
            }
        });

        button("STOP XOS", () ->
            stopService(new Intent(this,IslandService.class))
        );

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }
}
