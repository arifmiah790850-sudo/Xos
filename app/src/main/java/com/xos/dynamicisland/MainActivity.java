package com.xos.dynamicisland;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    LinearLayout root;

    void addButton(String name, Runnable action) {
        TextView b = new TextView(this);
        b.setText(name);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundColor(Color.rgb(35,35,35));
        b.setPadding(20,22,20,22);

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0,12,0,12);
        root.addView(b,p);
        b.setOnClickListener(v -> action.run());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(25,45,25,25);
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("XOS");
        title.setTextSize(36);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Dynamic Island  |  iOS Style");
        sub.setTextSize(14);
        sub.setTextColor(Color.LTGRAY);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        addButton("1. Allow Display Over Other Apps", () -> {
            startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        });

        addButton("2. Enable Usage Access", () -> {
            startActivity(new Intent(
                Settings.ACTION_USAGE_ACCESS_SETTINGS));
        });

        addButton("3. START XOS ISLAND", () -> {
            if (Settings.canDrawOverlays(this)) {
                Intent i = new Intent(this, IslandService.class);
                startForegroundService(i);
            } else {
                startActivity(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }
        });

        addButton("STOP XOS", () -> {
            stopService(new Intent(this, IslandService.class));
        });

        setContentView(root);
    }
}
