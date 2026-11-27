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
    SharedPreferences prefs;
    TextView positionInfo;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    void updateDot() {
        sendBroadcast(new Intent("com.xos.dynamicisland.UPDATE"));
    }

    void button(String name, Runnable action) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(Color.rgb(15, 75, 125))
        );
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, dp(50));
        p.setMargins(0, dp(4), 0, dp(4));
        root.addView(b, p);
        b.setOnClickListener(v -> action.run());
    }

    void heading(String name) {
        TextView t = new TextView(this);
        t.setText(name);
        t.setTextColor(Color.rgb(0, 190, 255));
        t.setTextSize(18);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(18), 0, dp(10));
        root.addView(t);
    }

    void refresh() {
        int x = prefs.getInt("dot_x", dp(16));
        int y = prefs.getInt("dot_y", dp(8));
        positionInfo.setText("Dot Position\nX: " + x + " px    Y: " + y + " px");
    }

    void change(String key, int amount, int min, int max) {
        int defaultValue = key.equals("dot_x") ? dp(16) : dp(8);
        int value = prefs.getInt(key, defaultValue);
        value = Math.max(min, Math.min(max, value + amount));
        prefs.edit().putInt(key, value).apply();
        refresh();
        updateDot();
    }

    void startDot() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:" + getPackageName())
            ));
            return;
        }

        Intent i = new Intent(this, IslandService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        prefs = getSharedPreferences("XOS_DOT_POSITION", MODE_PRIVATE);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundColor(Color.rgb(5, 12, 25));

        TextView title = new TextView(this);
        title.setText("XOS");
        title.setTextSize(36);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("NEON BLUE CAMERA DOT");
        subtitle.setTextColor(Color.rgb(0, 190, 255));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextSize(14);
        root.addView(subtitle);

        heading("DOT CONTROL");
        button("START BLUE DOT", this::startDot);
        button("STOP BLUE DOT", () ->
            stopService(new Intent(this, IslandService.class))
        );

        heading("POSITION CONTROL");
        button("▲  UP", () -> change("dot_y", -dp(4), 0, 3000));
        button("▼  DOWN", () -> change("dot_y", dp(4), 0, 3000));
        button("◀  LEFT", () -> change("dot_x", -dp(4), -1000, 1000));
        button("▶  RIGHT", () -> change("dot_x", dp(4), -1000, 1000));

        positionInfo = new TextView(this);
        positionInfo.setTextColor(Color.WHITE);
        positionInfo.setTextSize(16);
        positionInfo.setGravity(Gravity.CENTER);
        positionInfo.setPadding(0, dp(18), 0, dp(18));
        root.addView(positionInfo);
        refresh();

        button("RESET POSITION", () -> {
            prefs.edit()
                .putInt("dot_x", dp(16))
                .putInt("dot_y", dp(8))
                .apply();
            refresh();
            updateDot();
        });

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }
}
