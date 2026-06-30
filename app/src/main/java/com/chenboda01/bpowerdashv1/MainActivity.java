package com.chenboda01.bpowerdashv1;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    LinearLayout root;
    TextView percent, status, temp, voltage, health, plug, tip;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        build();
        refresh();
    }

    void build() {
        ScrollView sv = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(22), dp(22), dp(22));
        root.setBackgroundColor(Color.rgb(7,19,30));
        sv.addView(root);
        root.addView(tv("B-Battery V1", 31, Color.WHITE, true));
        root.addView(tv("Battery dashboard", 15, Color.LTGRAY, false));
        percent = tv("--%", 72, Color.rgb(80,255,157), true);
        status = card("Status", "--");
        temp = card("Temperature", "--");
        voltage = card("Voltage", "--");
        health = card("Health", "--");
        plug = card("Plugged", "--");
        tip = card("Tip", "--");
        root.addView(percent); root.addView(status); root.addView(temp); root.addView(voltage); root.addView(health); root.addView(plug); root.addView(tip);
        LinearLayout row1 = row();
        row1.addView(btn("Refresh", v -> refresh()));
        row1.addView(btn("Battery Settings", v -> openBatterySettings()));
        root.addView(row1);
        LinearLayout row2 = row();
        row2.addView(btn("B-Launcher", v -> openApp("com.chenboda01.blauncherv1","com.chenboda01.blauncherv1.MainActivity","B-Launcher")));
        row2.addView(btn("B-Clock", v -> openApp("com.chenboda01.bclockv1","com.chenboda01.bclockv1.MainActivity","B-Clock")));
        root.addView(row2);
        setContentView(sv);
    }

    TextView card(String a, String b) {
        TextView t = tv(a + ": " + b, 19, Color.WHITE, false);
        t.setBackgroundColor(Color.rgb(19,40,56));
        t.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(7), 0, dp(7));
        t.setLayoutParams(p);
        return t;
    }
    void setCard(TextView t, String a, String b) { t.setText(a + ": " + b); }
    TextView tv(String s, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER); t.setPadding(4, 8, 4, 8); if (bold) t.setTypeface(null, 1); return t; }
    Button btn(String s, View.OnClickListener l) { Button b = new Button(this); b.setText(s); b.setAllCaps(false); b.setOnClickListener(l); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(58), 1); p.setMargins(dp(5), dp(5), dp(5), dp(5)); b.setLayoutParams(p); return b; }
    LinearLayout row() { LinearLayout r = new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); return r; }
    int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }

    void refresh() {
        Intent i = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (i == null) return;
        int level = i.getIntExtra("level", -1);
        int scale = i.getIntExtra("scale", 100);
        int pct = Math.round(level * 100f / scale);
        int st = i.getIntExtra("status", -1);
        int h = i.getIntExtra("health", -1);
        int p = i.getIntExtra("plugged", 0);
        int temp10 = i.getIntExtra("temperature", -1);
        int mv = i.getIntExtra("voltage", -1);
        percent.setText(pct + "%");
        percent.setTextColor(colorFor(pct, st));
        setCard(status, "Status", statusText(st));
        setCard(temp, "Temperature", temp10 >= 0 ? String.format("%.1f C / %.1f F", temp10/10f, temp10/10f*9/5+32) : "--");
        setCard(voltage, "Voltage", mv > 0 ? mv + " mV" : "--");
        setCard(health, "Health", healthText(h));
        setCard(plug, "Plugged", plugText(p));
        setCard(tip, "Tip", tipText(pct, st, temp10));
    }

    int colorFor(int pct, int st) { if (st == 2 || st == 5) return Color.rgb(80,255,157); if (pct <= 15) return Color.rgb(255,80,80); if (pct <= 35) return Color.rgb(255,190,70); return Color.rgb(80,255,157); }
    String statusText(int s) { if (s == 2) return "Charging"; if (s == 3) return "Discharging"; if (s == 4) return "Not charging"; if (s == 5) return "Full"; return "Unknown"; }
    String healthText(int h) { if (h == 2) return "Good"; if (h == 3) return "Overheat"; if (h == 4) return "Dead"; if (h == 5) return "Over voltage"; if (h == 6) return "Failure"; if (h == 7) return "Cold"; return "Unknown"; }
    String plugText(int p) { if (p == 1) return "AC charger"; if (p == 2) return "USB"; if (p == 4) return "Wireless"; return "Not plugged"; }
    String tipText(int pct, int st, int temp10) { if (temp10 > 390) return "Phone is hot. Stop heavy apps."; if (pct <= 15) return "Low battery. Turn on battery saver."; if (st == 2 && pct >= 80) return "Charged enough. Unplug soon."; if (st == 2) return "Charging. Avoid gaming while charging."; return "Dim screen to save power."; }
    void openBatterySettings() { try { startActivity(new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)); } catch(Exception e) { try { startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch(Exception ex) { toast("Could not open settings."); } } }
    void openApp(String pkg, String cls, String label) { try { Intent launch = getPackageManager().getLaunchIntentForPackage(pkg); if (launch == null) { launch = new Intent(Intent.ACTION_MAIN); launch.addCategory(Intent.CATEGORY_LAUNCHER); launch.setClassName(pkg, cls); } startActivity(launch); } catch(Exception e) { toast(label + " is not installed yet."); } }
    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
