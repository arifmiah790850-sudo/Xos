package com.xos.dynamicisland;

import android.app.*;
import android.os.*;
import android.content.*;
import android.app.usage.*;
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
    Handler handler = new Handler();
    boolean expanded = false;
    boolean added = false;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel channel = new NotificationChannel(
            "xos_channel", "XOS Island",
            NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class)
            .createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "xos_channel")
            .setContentTitle("XOS Dynamic Island")
            .setContentText("Island is active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
        }

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        createIsland();
        handler.post(poll);
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
            0, dp(20), 1));

        LinearLayout.LayoutParams dotParams =
            new LinearLayout.LayoutParams(dp(8), dp(8));
        dotParams.setMargins(dp(8), 0, 0, 0);
        island.addView(dot, dotParams);

        int type = Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
            dp(112), dp(34), type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = dp(5);

        island.setOnClickListener(v -> toggleExpand());

        added = true;
        updatePosition();

        try {
            wm.addView(island, params);
        } catch (Exception e) {
            added = false;
        }
    }

    void toggleExpand() {
        expanded = !expanded;

        if (expanded) {
            title.setText("XOS  •  LIVE");
            params.width = dp(220);
            params.height = dp(48);
        } else {
            title.setText("XOS");
            params.width = dp(112);
            params.height = dp(34);
        }

        if (added) {
            try {
                wm.updateViewLayout(island, params);
            } catch (Exception ignored) {}
        }
    }

    void updatePosition() {
        if (params == null) return;

        boolean landscape =
            getResources().getConfiguration().orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE;

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = dp(landscape ? 3 : 5);
    }

    Runnable poll = new Runnable() {
        @Override
        public void run() {
            try {
                updatePosition();

                if (SettingsHelper.allowed(IslandService.this)) {
                    UsageStatsManager usage =
                        (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);

                    long now = System.currentTimeMillis();
                    UsageEvents events = usage.queryEvents(now - 10000, now);
                    UsageEvents.Event event = new UsageEvents.Event();
                    String foreground = "";

                    while (events.hasNextEvent()) {
                        events.getNextEvent(event);
                        if (event.getEventType() ==
                            UsageEvents.Event.ACTIVITY_RESUMED ||
                            event.getEventType() ==
                            UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            foreground = event.getPackageName();
                        }
                    }

                    boolean show = !foreground.isEmpty() &&
                        !foreground.equals(getPackageName());

                    if (added) {
                        island.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                }
            } catch (Exception ignored) {}

            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(poll);
        if (added && island != null) {
            try { wm.removeView(island); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class SettingsHelper {
        static boolean allowed(Context c) {
            try {
                android.app.AppOpsManager appOps =
                    (android.app.AppOpsManager)c.getSystemService(
                        Context.APP_OPS_SERVICE);
                int mode = appOps.checkOpNoThrow(
                    "android:get_usage_stats",
                    android.os.Process.myUid(), c.getPackageName());
                return mode == android.app.AppOpsManager.MODE_ALLOWED;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
