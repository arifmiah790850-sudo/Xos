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

    int dp(int n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    void addButton(String name, Runnable action) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(Color.rgb(35,35,35))
        );

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, dp(52));
        p.setMargins(dp(4),dp(5),dp(4),dp(5));
        root.addView(b,p);
        b.setOnClickListener(v -> action.run());
    }

    void move(int dx, int dy) {
        int x = prefs.getInt("x",0) + dp(dx);
        int y = prefs.getInt("y",dp(5)) + dp(dy);

        int maxY = getResources().getDisplayMetrics().heightPixels - dp(70);
        y = Math.max(0,Math.min(y,maxY));

        prefs.edit().putInt("x",x).putInt("y",y).apply();
        Toast.makeText(this,"XOS Position Saved",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("XOS_POSITION",MODE_PRIVATE);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(25),dp(18),dp(20));
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("XOS");
        title.setTextSize(34);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("DYNAMIC ISLAND 26");
        sub.setTextColor(Color.LTGRAY);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        addButton("ALLOW DISPLAY OVER OTHER APPS", () ->
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:"+getPackageName()))));

        addButton("ENABLE USAGE ACCESS", () ->
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));

        TextView heading = new TextView(this);
        heading.setText("\nISLAND POSITION SETTINGS");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(18);
        heading.setGravity(Gravity.CENTER);
        root.addView(heading);

        addButton("▲  UP", () -> move(0,-20));
        addButton("◀  LEFT", () -> move(-20,0));
        addButton("▶  RIGHT", () -> move(20,0));
        addButton("▼  DOWN", () -> move(0,20));

        addButton("RESET POSITION", () -> {
            prefs.edit().putInt("x",0).putInt("y",dp(5)).apply();
            Toast.makeText(this,"Position Reset",Toast.LENGTH_SHORT).show();
        });

        addButton("START XOS ISLAND", () -> {
            if (Settings.canDrawOverlays(this)) {
                Intent i = new Intent(this,IslandService.class);
                if (Build.VERSION.SDK_INT >= 26)
                    startForegroundService(i);
                else startService(i);
            } else {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:"+getPackageName())));
            }
        });

        addButton("STOP XOS", () ->
            stopService(new Intent(this,IslandService.class)));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }
}
