package com.xos.dynamicisland;

import android.app.*;
import android.os.*;
import android.content.*;
import android.view.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;

public class IslandService extends Service {

    WindowManager wm;
    LinearLayout island;
    TextView title;
    View dot;
    WindowManager.LayoutParams params;
    android.content.SharedPreferences prefs;

    boolean expanded = false;
    boolean added = false;
    boolean receiverRegistered = false;

    final String ACTION_UPDATE = "com.xos.dynamicisland.UPDATE";

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updatePosition();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getSharedPreferences("XOS_POSITION", MODE_PRIVATE);

        NotificationChannel channel = new NotificationChannel(
            "xos_channel",
            "XOS Island",
            NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class)
            .createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "xos_channel")
            .setContentTitle("XOS Dynamic Island")
            .setContentText("Island is active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
        }

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);

        createIsland();

        IntentFilter filter = new IntentFilter(ACTION_UPDATE);

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }

        receiverRegistered = true;
    }

    void createIsland() {

        island = new LinearLayout(this);
        island.setOrientation(LinearLayout.HORIZONTAL);
        island.setGravity(Gravity.CENTER_VERTICAL);
        island.setPadding(dp(15), 0, dp(12), 0);
        island.setBackground(shape(Color.BLACK, 40));

        title = new TextView(this);
        title.setText("XOS");
        title.setTextColor(Color.WHITE);
        title.setTextSize(12);
        title.setTypeface(null, Typeface.BOLD);

        dot = new View(this);
        dot.setBackground(shape(Color.rgb(0,255,100), 20));

        island.addView(title, new LinearLayout.LayoutParams(
            0, dp(20), 1
        ));

        LinearLayout.LayoutParams dotParams =
            new LinearLayout.LayoutParams(dp(8), dp(8));

        dotParams.setMargins(dp(8), 0, 0, 0);
        island.addView(dot, dotParams);

        int type = Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
            dp(prefs.getInt("width",112)),
            dp(prefs.getInt("height",34)),
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        island.setOnClickListener(v -> toggleExpand());

        updatePosition();

        try {
            wm.addView(island, params);
            added = true;
        } catch (Exception e) {
            added = false;
        }
    }

    void updatePosition() {
        if (params == null) return;

        int x = prefs.getInt("x",0);
        int y = prefs.getInt("y",dp(5));
        int width = prefs.getInt("width",112);
        int height = prefs.getInt("height",34);

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.x = x;
        params.y = y;

        params.width = dp(expanded ? Math.max(width,220) : width);
        params.height = dp(expanded ? Math.max(height,48) : height);

        if (added && island != null) {
            try {
                wm.updateViewLayout(island,params);
            } catch (Exception ignored) {}
        }
    }

    void toggleExpand() {
        expanded = !expanded;

        if (expanded) {
            title.setText("XOS  •  LIVE");
            island.setPadding(dp(18),0,dp(15),0);
        } else {
            title.setText("XOS");
            island.setPadding(dp(15),0,dp(12),0);
        }

        updatePosition();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (receiverRegistered) {
            try {
                unregisterReceiver(updateReceiver);
            } catch (Exception ignored) {}
        }

        if (added && island != null) {
            try {
                wm.removeView(island);
            } catch (Exception ignored) {}
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
