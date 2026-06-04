package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;

public class ShoofSplashActivity extends Activity implements NotificationCenter.NotificationCenterDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        buildUI();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (UserConfig.getInstance(UserConfig.selectedAccount).isClientActivated()) {
                    openCrepixbot();
                    return;
                }
            } catch (Exception ignored) {}
            openTelegramLogin();
        }, 500);
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#080808"));
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 0, 64, 0);

        TextView icon = new TextView(this);
        icon.setText("🎬");
        icon.setTextSize(72);
        icon.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("شوف");
        title.setTextColor(Color.WHITE);
        title.setTextSize(56);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("TV");
        subtitle.setTextColor(Color.parseColor("#E5001A"));
        subtitle.setTextSize(28);
        subtitle.setTypeface(null, Typeface.BOLD);
        subtitle.setGravity(Gravity.CENTER);

        TextView desc = new TextView(this);
        desc.setText("شاهد آلاف المسلسلات والأنيميات\nبجودة عالية في أي مكان");
        desc.setTextColor(Color.parseColor("#99FFFFFF"));
        desc.setTextSize(14);
        desc.setGravity(Gravity.CENTER);

        View spacer = new View(this);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 80);
        spacer.setLayoutParams(sp);

        Button btnStart = new Button(this);
        btnStart.setText("ابدأ المشاهدة مجاناً");
        btnStart.setTextColor(Color.WHITE);
        btnStart.setTextSize(16);
        btnStart.setBackgroundColor(Color.parseColor("#E5001A"));
        btnStart.setAllCaps(false);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
        btnParams.setMargins(0, 40, 0, 0);
        btnStart.setLayoutParams(btnParams);
        btnStart.setOnClickListener(v -> openTelegramLogin());

        TextView hint = new TextView(this);
        hint.setText("يتطلب حساب تيليجرام");
        hint.setTextColor(Color.parseColor("#44FFFFFF"));
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hintP.setMargins(0, 16, 0, 0);
        hint.setLayoutParams(hintP);

        root.addView(icon);
        root.addView(title);
        root.addView(subtitle);
        root.addView(desc);
        root.addView(spacer);
        root.addView(btnStart);
        root.addView(hint);
        setContentView(root);
    }

    private void openTelegramLogin() {
        try {
            NotificationCenter.getInstance(UserConfig.selectedAccount)
                .addObserver(this, NotificationCenter.mainUserInfoChanged);
        } catch (Exception ignored) {}
        Intent intent = new Intent(this, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.mainUserInfoChanged) {
            try {
                if (UserConfig.getInstance(account).isClientActivated()) {
                    runOnUiThread(this::openCrepixbot);
                }
            } catch (Exception ignored) {}
        }
    }

    private void openCrepixbot() {
        try {
            NotificationCenter.getInstance(UserConfig.selectedAccount)
                .removeObserver(this, NotificationCenter.mainUserInfoChanged);
        } catch (Exception ignored) {}
        Intent intent = new Intent(this, LaunchActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("tg://resolve?domain=Crepixbot"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            NotificationCenter.getInstance(UserConfig.selectedAccount)
                .removeObserver(this, NotificationCenter.mainUserInfoChanged);
        } catch (Exception ignored) {}
    }
}
