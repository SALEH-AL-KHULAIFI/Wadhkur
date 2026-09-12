package com.saleh.wadhkur;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(4, 8, 15);
    private static final int CARD = Color.rgb(9, 20, 31);
    private static final int GREEN = Color.rgb(57, 255, 143);
    private static final int CYAN = Color.rgb(55, 220, 255);
    private static final int PURPLE = Color.rgb(169, 92, 255);
    private static final int WHITE = Color.rgb(239, 250, 245);
    private static final int MUTED = Color.rgb(150, 170, 182);
    private static final int RED = Color.rgb(255, 85, 105);

    private static final int LOCATION_REQUEST_CODE = 501;
    private static final int NOTIFICATION_REQUEST_CODE = 502;

    private static final String PREFS = "settings";

    private static final String GENERAL_ENABLED = "general_enabled";
    private static final String MORNING_ENABLED = "morning_enabled";
    private static final String EVENING_ENABLED = "evening_enabled";

    private static final String GENERAL_HOUR = "general_hour";
    private static final String GENERAL_MINUTE = "general_minute";

    private static final String MORNING_HOUR = "morning_hour";
    private static final String MORNING_MINUTE = "morning_minute";

    private static final String EVENING_HOUR = "evening_hour";
    private static final String EVENING_MINUTE = "evening_minute";

    private static final String LATITUDE = "prayer_latitude";
    private static final String LONGITUDE = "prayer_longitude";

    private SharedPreferences prefs;

    private TextView countText;
    private TextView statusText;

    private int currentIndex = 0;
    private DhikrData.Dhikr[] currentData;
    private String currentTitle;

    private TextView dhikrCounterText;
    private TextView dhikrPositionText;
    private TextView dhikrTitleText;
    private TextView dhikrBodyText;
    private TextView dhikrSourceText;
    private Button dhikrActionButton;

    private int currentRepeat = 0;

    private float downX;
    private float downY;

    private final Handler countdownHandler =
            new Handler(Looper.getMainLooper());

    private TextView ramadanCountdownText;

    private final Runnable countdownRunnable =
            new Runnable() {
                @Override
                public void run() {

                    if (ramadanCountdownText != null) {

                        updateRamadanCountdown();

                        countdownHandler.postDelayed(
                                this,
                                1000
                        );
                    }
                }
            };

    @Override
    protected void onCreate(Bundle b) {

        super.onCreate(b);

        prefs = getSharedPreferences(
                PREFS,
                MODE_PRIVATE
        );

        initializeReminderDefaults();

        showHome();

        handleNotificationIntent(
                getIntent()
        );

        requestNotificationPermissionIfNeeded();

        scheduleEnabledReminders();

        requestLocationPermissionIfNeeded();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        setIntent(intent);

        handleNotificationIntent(
                intent
        );
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (prefs != null) {

            scheduleEnabledReminders();
        }
    }

    @Override
    protected void onDestroy() {

        countdownHandler.removeCallbacks(
                countdownRunnable
        );

        super.onDestroy();
    }

    /*
     * الإعدادات الافتراضية
     */
    private void initializeReminderDefaults() {

        SharedPreferences.Editor editor =
                prefs.edit();

        boolean changed = false;

        if (!prefs.contains(GENERAL_ENABLED)) {

            editor.putBoolean(
                    GENERAL_ENABLED,
                    true
            );

            changed = true;
        }

        if (!prefs.contains(MORNING_ENABLED)) {

            editor.putBoolean(
                    MORNING_ENABLED,
                    true
            );

            changed = true;
        }

        if (!prefs.contains(EVENING_ENABLED)) {

            editor.putBoolean(
                    EVENING_ENABLED,
                    true
            );

            changed = true;
        }

        if (!prefs.contains(GENERAL_HOUR)) {

            editor.putInt(
                    GENERAL_HOUR,
                    12
            );

            editor.putInt(
                    GENERAL_MINUTE,
                    0
            );

            changed = true;
        }

        if (!prefs.contains(MORNING_HOUR)) {

            editor.putInt(
                    MORNING_HOUR,
                    6
            );

            editor.putInt(
                    MORNING_MINUTE,
                    0
            );

            changed = true;
        }

        if (!prefs.contains(EVENING_HOUR)) {

            editor.putInt(
                    EVENING_HOUR,
                    17
            );

            editor.putInt(
                    EVENING_MINUTE,
                    0
            );

            changed = true;
        }

        if (changed) {

            editor.apply();
        }
    }

    /*
     * إذن الإشعارات
     */
    private void requestNotificationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_REQUEST_CODE
                );
            }
        }
    }

    /*
     * إذن الموقع
     */
    private void requestLocationPermissionIfNeeded() {

        if (Build.VERSION.SDK_INT < 23) {

            loadLastKnownLocation();

            return;
        }

        boolean fine =
                checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarse =
                checkSelfPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {

            loadLastKnownLocation();

            return;
        }

        requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_REQUEST_CODE) {

            boolean granted = false;

            for (int result : grantResults) {

                if (result ==
                        PackageManager.PERMISSION_GRANTED) {

                    granted = true;
                    break;
                }
            }

            if (granted) {

                loadLastKnownLocation();

                Toast.makeText(
                        this,
                        "تم السماح بالموقع لحساب مواقيت الصلاة",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "لم يتم السماح بالموقع، ويمكنك تفعيله من الإعدادات",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    /*
     * جدولة جميع التنبيهات المفعلة
     */
    private void scheduleEnabledReminders() {

        ReminderScheduler.scheduleAll(
                this
        );
    }

    /*
     * التعامل مع الإشعار
     */
    private void handleNotificationIntent(
            Intent intent
    ) {

        if (intent == null) {
            return;
        }

        String section =
                intent.getStringExtra(
                        "open_section"
                );

        if (section == null) {
            return;
        }

        if (ReminderScheduler.TYPE_MORNING.equals(section)) {

            section(
                    "🌅 أذكار الصباح",
                    DhikrData.MORNING
            );

        } else if (
                ReminderScheduler.TYPE_EVENING.equals(section)
        ) {

            section(
                    "🌙 أذكار المساء",
                    DhikrData.EVENING
            );

        } else if (
                ReminderScheduler.TYPE_GENERAL.equals(section)
        ) {

            DhikrData.Dhikr[] data =
                    getGeneralDhikrData();

            section(
                    "🔔 التذكير العام",
                    data
            );
        }
    }

    /*
     * بيانات التذكير العام
     *
     * نستخدم الأذكار الموجودة أصلًا في التطبيق
     * حتى يكون كل إشعار ذكرًا كاملًا.
     */
    private DhikrData.Dhikr[] getGeneralDhikrData() {

        int morningLength =
                DhikrData.MORNING == null
                        ? 0
                        : DhikrData.MORNING.length;

        int eveningLength =
                DhikrData.EVENING == null
                        ? 0
                        : DhikrData.EVENING.length;

        DhikrData.Dhikr[] result =
                new DhikrData.Dhikr[
                        morningLength + eveningLength
                ];

        int index = 0;

        if (DhikrData.MORNING != null) {

            for (DhikrData.Dhikr item :
                    DhikrData.MORNING) {

                result[index++] = item;
            }
        }

        if (DhikrData.EVENING != null) {

            for (DhikrData.Dhikr item :
                    DhikrData.EVENING) {

                result[index++] = item;
            }
        }

        return result;
    }

    /*
     * الصفحة الرئيسية
     */
    private void showHome() {

        currentData = null;
        currentTitle = null;
        currentIndex = 0;
        currentRepeat = 0;

        setContentView(
                R.layout.activity_main
        );

        countText =
                findViewById(
                        R.id.countText
                );

        statusText =
                findViewById(
                        R.id.statusText
                );

        View morning =
                findViewById(
                        R.id.morningBtn
                );

        if (morning != null) {

            morning.setOnClickListener(
                    v -> section(
                            "🌅 أذكار الصباح",
                            DhikrData.MORNING
                    )
            );
        }

        View evening =
                findViewById(
                        R.id.eveningBtn
                );

        if (evening != null) {

            evening.setOnClickListener(
                    v -> section(
                            "🌙 أذكار المساء",
                            DhikrData.EVENING
                    )
            );
        }

        View duas =
                findViewById(
                        R.id.duasBtn
                );

        if (duas != null) {

            duas.setOnClickListener(
                    v -> section(
                            "🤲 الأدعية",
                            DhikrData.DUAS
                    )
            );
        }

        View tasbeeh =
                findViewById(
                        R.id.tasbeehBtn
                );

        if (tasbeeh != null) {

            tasbeeh.setOnClickListener(
                    v -> tasbeeh()
            );
        }

        View prayer =
                findViewById(
                        R.id.prayerBtn
                );

        if (prayer != null) {

            prayer.setOnClickListener(
                    v -> prayerTimes()
            );
        }

        View settings =
                findViewById(
                        R.id.settingsBtn
                );

        if (settings != null) {

            settings.setOnClickListener(
                    v -> settings()
            );
        }

        View about =
                findViewById(
                        R.id.aboutBtn
                );

        if (about != null) {

            about.setOnClickListener(
                    v -> about()
            );
        }

        refresh();

        addRamadanCountdown();
    }

    /*
     * عداد رمضان
     */
    private void addRamadanCountdown() {

        View content =
                ((ViewGroup) findViewById(
                        android.R.id.content
                )).getChildAt(0);

        if (!(content instanceof ScrollView)) {
            return;
        }

        ScrollView scrollView =
                (ScrollView) content;

        if (!(scrollView.getChildAt(0)
                instanceof LinearLayout)) {

            return;
        }

        LinearLayout root =
                (LinearLayout) scrollView.getChildAt(0);

        LinearLayout ramadanCard =
                new LinearLayout(this);

        ramadanCard.setOrientation(
                LinearLayout.VERTICAL
        );

        ramadanCard.setGravity(
                Gravity.CENTER
        );

        ramadanCard.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        ramadanCard.setBackgroundResource(
                R.drawable.card_bg
        );

        ramadanCard.setElevation(
                dp(4)
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        cardParams.setMargins(
                0,
                dp(8),
                0,
                dp(2)
        );

        ramadanCard.setLayoutParams(
                cardParams
        );

        TextView title =
                label(
                        "🌙  العد التنازلي لرمضان 1448",
                        GREEN,
                        16,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        ramadanCard.addView(
                title
        );

        ramadanCountdownText =
                label(
                        "جاري الحساب...",
                        CYAN,
                        21,
                        true
                );

        ramadanCountdownText.setGravity(
                Gravity.CENTER
        );

        ramadanCountdownText.setPadding(
                0,
                dp(5),
                0,
                dp(2)
        );

        ramadanCard.addView(
                ramadanCountdownText
        );

        TextView note =
                label(
                        "الموعد المتوقع: 8 فبراير 2027 • قد يختلف حسب رؤية الهلال",
                        MUTED,
                        10,
                        false
                );

        note.setGravity(
                Gravity.CENTER
        );

        ramadanCard.addView(
                note
        );

        root.addView(
                ramadanCard,
                1
        );

        updateRamadanCountdown();

        countdownHandler.removeCallbacks(
                countdownRunnable
        );

        countdownHandler.postDelayed(
                countdownRunnable,
                1000
        );
    }

    private void updateRamadanCountdown() {

        if (ramadanCountdownText == null) {
            return;
        }

        Calendar target =
                Calendar.getInstance();

        target.set(
                2027,
                Calendar.FEBRUARY,
                8,
                0,
                0,
                0
        );

        target.set(
                Calendar.MILLISECOND,
                0
        );

        long difference =
                target.getTimeInMillis()
                        - System.currentTimeMillis();

        if (difference <= 0) {

            ramadanCountdownText.setText(
                    "🌙 رمضان 1448 بدأ — رمضان كريم"
            );

            return;
        }

        long totalSeconds =
                difference / 1000;

        long days =
                totalSeconds / 86400;

        totalSeconds %= 86400;

        long hours =
                totalSeconds / 3600;

        totalSeconds %= 3600;

        long minutes =
                totalSeconds / 60;

        long seconds =
                totalSeconds % 60;

        ramadanCountdownText.setText(
                "باقي " +
                        days +
                        " يوم  •  " +
                        String.format(
                                Locale.US,
                                "%02d:%02d:%02d",
                                hours,
                                minutes,
                                seconds
                        )
        );
    }

    /*
     * تحديث الصفحة الرئيسية
     */
    private void refresh() {

        if (countText == null ||
                statusText == null) {

            return;
        }

        String d = today();

        if (!d.equals(
                prefs.getString(
                        "date",
                        ""
                )
        )) {

            prefs.edit()
                    .putString(
                            "date",
                            d
                    )
                    .putInt(
                            "count",
                            0
                    )
                    .apply();
        }

        countText.setText(
                String.valueOf(
                        prefs.getInt(
                                "count",
                                0
                        )
                )
        );

        boolean general =
                prefs.getBoolean(
                        GENERAL_ENABLED,
                        true
                );

        boolean morning =
                prefs.getBoolean(
                        MORNING_ENABLED,
                        true
                );

        boolean evening =
                prefs.getBoolean(
                        EVENING_ENABLED,
                        true
                );

        int enabledCount = 0;

        if (general) {
            enabledCount++;
        }

        if (morning) {
            enabledCount++;
        }

        if (evening) {
            enabledCount++;
        }

        if (enabledCount == 3) {

            statusText.setText(
                    "🟢 جميع التذكيرات مفعلة"
            );

            statusText.setTextColor(
                    GREEN
            );

        } else if (enabledCount > 0) {

            statusText.setText(
                    "🟢 التذكيرات المفعلة: " +
                            enabledCount +
                            " من 3"
            );

            statusText.setTextColor(
                    GREEN
            );

        } else {

            statusText.setText(
                    "⚪ جميع التذكيرات متوقفة"
            );

            statusText.setTextColor(
                    MUTED
            );
        }
    }

    /*
     * شاشة الأذكار
     */
    private void section(
            String title,
            DhikrData.Dhikr[] data
    ) {

        if (data == null ||
                data.length == 0) {

            Toast.makeText(
                    this,
                    "لا توجد أذكار في هذه المجموعة",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        currentTitle = title;
        currentData = data;
        currentIndex = 0;
        currentRepeat = 0;

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                BG
        );

        root.setPadding(
                dp(12),
                dp(7),
                dp(12),
                dp(10)
        );

        root.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView back =
                label(
                        "‹",
                        GREEN,
                        42,
                        true
                );

        back.setGravity(
                Gravity.CENTER
        );

        back.setOnClickListener(
                v -> showHome()
        );

        header.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(58)
                )
        );

        TextView titleText =
                label(
                        title,
                        WHITE,
                        22,
                        true
                );

        titleText.setGravity(
                Gravity.CENTER
        );

        header.addView(
                titleText,
                new LinearLayout.LayoutParams(
                        0,
                        dp(58),
                        1
                )
        );

        root.addView(
                header
        );

        TextView instruction =
                label(
                        "اسحب يمينًا أو يسارًا للتنقل بين الأذكار",
                        MUTED,
                        12,
                        false
                );

        instruction.setGravity(
                Gravity.CENTER
        );

        instruction.setPadding(
                0,
                0,
                0,
                dp(7)
        );

        root.addView(
                instruction
        );

        dhikrPositionText =
                label(
                        "",
                        CYAN,
                        15,
                        true
                );

        dhikrPositionText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                dhikrPositionText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(34)
                )
        );

        FrameLayout cardContainer =
                new FrameLayout(this);

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        cardContainer.setLayoutParams(
                containerParams
        );

        ScrollView cardScroll =
                new ScrollView(this);

        cardScroll.setFillViewport(
                true
        );

        cardScroll.setBackgroundColor(
                BG
        );

        LinearLayout cardHolder =
                new LinearLayout(this);

        cardHolder.setOrientation(
                LinearLayout.VERTICAL
        );

        cardHolder.setGravity(
                Gravity.CENTER
        );

        cardHolder.setPadding(
                dp(2),
                dp(4),
                dp(2),
                dp(8)
        );

        cardScroll.addView(
                cardHolder
        );

        cardContainer.addView(
                cardScroll
        );

        root.addView(
                cardContainer
        );

        LinearLayout navigation =
                new LinearLayout(this);

        navigation.setGravity(
                Gravity.CENTER
        );

        navigation.setPadding(
                0,
                dp(7),
                0,
                0
        );

        Button previous =
                createActionButton(
                        "‹  السابق"
                );

        Button next =
                createActionButton(
                        "التالي  ›"
                );

        LinearLayout.LayoutParams navParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                );

        navParams.setMargins(
                dp(4),
                0,
                dp(4),
                0
        );

        navigation.addView(
                previous,
                navParams
        );

        navigation.addView(
                next,
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                )
        );

        root.addView(
                navigation
        );

        previous.setOnClickListener(
                v -> {

                    if (currentIndex > 0) {

                        currentIndex--;
                        currentRepeat = 0;

                        showCurrentDhikr(
                                cardHolder
                        );

                        cardScroll.scrollTo(
                                0,
                                0
                        );

                    } else {

                        Toast.makeText(
                                this,
                                "هذا أول ذكر",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        next.setOnClickListener(
                v -> {

                    if (currentIndex <
                            currentData.length - 1) {

                        currentIndex++;
                        currentRepeat = 0;

                        showCurrentDhikr(
                                cardHolder
                        );

                        cardScroll.scrollTo(
                                0,
                                0
                        );

                    } else {

                        Toast.makeText(
                                this,
                                "أكملت آخر ذكر في المجموعة",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        cardContainer.setOnTouchListener(
                (v, event) -> {

                    switch (event.getActionMasked()) {

                        case MotionEvent.ACTION_DOWN:

                            downX = event.getX();
                            downY = event.getY();

                            return true;

                        case MotionEvent.ACTION_UP:

                            float deltaX =
                                    event.getX() - downX;

                            float deltaY =
                                    event.getY() - downY;

                            if (Math.abs(deltaX) >
                                    dp(70) &&
                                    Math.abs(deltaX) >
                                    Math.abs(deltaY) * 1.3f) {

                                if (deltaX < 0) {

                                    if (currentIndex <
                                            currentData.length - 1) {

                                        currentIndex++;
                                        currentRepeat = 0;

                                        showCurrentDhikr(
                                                cardHolder
                                        );

                                        cardScroll.scrollTo(
                                                0,
                                                0
                                        );

                                    } else {

                                        Toast.makeText(
                                                this,
                                                "هذا آخر ذكر",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                } else {

                                    if (currentIndex > 0) {

                                        currentIndex--;
                                        currentRepeat = 0;

                                        showCurrentDhikr(
                                                cardHolder
                                        );

                                        cardScroll.scrollTo(
                                                0,
                                                0
                                        );

                                    } else {

                                        Toast.makeText(
                                                this,
                                                "هذا أول ذكر",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }

                                return true;
                            }

                            return true;

                        case MotionEvent.ACTION_CANCEL:

                            return true;
                    }

                    return true;
                }
        );

        setContentView(
                root
        );

        showCurrentDhikr(
                cardHolder
        );
    }

    /*
     * عرض الذكر الحالي
     */
    private void showCurrentDhikr(
            LinearLayout holder
    ) {

        if (currentData == null ||
                currentData.length == 0) {

            return;
        }

        holder.removeAllViews();

        DhikrData.Dhikr dhikr =
                currentData[currentIndex];

        dhikrPositionText.setText(
                (currentIndex + 1) +
                        " من " +
                        currentData.length
        );

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

        card.setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(20)
        );

        card.setBackgroundResource(
                R.drawable.card_bg
        );

        card.setElevation(
                dp(8)
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        cardParams.setMargins(
                dp(2),
                dp(4),
                dp(2),
                dp(8)
        );

        card.setLayoutParams(
                cardParams
        );

        dhikrTitleText =
                label(
                        "✦  " +
                                dhikr.title +
                                "  ✦",
                        GREEN,
                        19,
                        true
                );

        dhikrTitleText.setGravity(
                Gravity.CENTER
        );

        card.addView(
                dhikrTitleText
        );

        dhikrBodyText =
                label(
                        dhikr.text,
                        WHITE,
                        22,
                        false
                );

        dhikrBodyText.setGravity(
                Gravity.CENTER
        );

        dhikrBodyText.setLineSpacing(
                dp(5),
                1.12f
        );

        dhikrBodyText.setPadding(
                dp(3),
                dp(18),
                dp(3),
                dp(14)
        );

        card.addView(
                dhikrBodyText
        );

        if (dhikr.source != null &&
                !dhikr.source.trim().isEmpty()) {

            dhikrSourceText =
                    label(
                            "📚 " + dhikr.source,
                            MUTED,
                            12,
                            false
                    );

            dhikrSourceText.setGravity(
                    Gravity.CENTER
            );

            dhikrSourceText.setPadding(
                    0,
                    dp(2),
                    0,
                    dp(10)
            );

            card.addView(
                    dhikrSourceText
            );
        }

        int targetRepeat =
                Math.max(
                        1,
                        dhikr.count
                );

        dhikrCounterText =
                label(
                        progress(
                                currentRepeat,
                                targetRepeat
                        ),
                        CYAN,
                        19,
                        true
                );

        dhikrCounterText.setGravity(
                Gravity.CENTER
        );

        card.addView(
                dhikrCounterText
        );

        dhikrActionButton =
                createActionButton(
                        currentRepeat >= targetRepeat
                                ? "✓  مكتمل"
                                : "تسبيح / تكرار"
                );

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56)
                );

        actionParams.setMargins(
                dp(2),
                dp(10),
                dp(2),
                0
        );

        card.addView(
                dhikrActionButton,
                actionParams
        );

        dhikrActionButton.setOnClickListener(
                v -> {

                    if (currentRepeat <
                            targetRepeat) {

                        currentRepeat++;

                        recordDhikr(
                                this
                        );

                        dhikrCounterText.setText(
                                progress(
                                        currentRepeat,
                                        targetRepeat
                                )
                        );

                        if (currentRepeat >=
                                targetRepeat) {

                            dhikrActionButton.setText(
                                    "✓  مكتمل"
                            );

                            Toast.makeText(
                                    this,
                                    "أحسنت، اكتمل الذكر",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );

        holder.addView(
                card
        );
    }

    /*
     * عداد التسبيح
     */
    private void tasbeeh() {

        final String[] names = {
                "سبحان الله",
                "الحمد لله",
                "الله أكبر",
                "سبحان الله العظيم",
                "أستغفر الله العظيم وأتوب إليه",
                "اللهم صل وسلم على نبينا محمد",
                "لا إله إلا أنت سبحانك إني كنت من الظالمين",
                "لا حول ولا قوة إلا بالله العظيم"
        };

        final int[] value = {
                prefs.getInt(
                        "tasbeeh",
                        0
                )
        };

        final int[] selected = {0};

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                dp(18),
                dp(12),
                dp(18),
                dp(18)
        );

        root.setBackgroundColor(
                BG
        );

        TextView title =
                label(
                        "📿  عداد التسبيح",
                        GREEN,
                        25,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        TextView selectedText =
                label(
                        names[selected[0]],
                        WHITE,
                        21,
                        true
                );

        selectedText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                selectedText
        );

        Button choose =
                new Button(this);

        choose.setText(
                "اختيار الذكر"
        );

        choose.setTextColor(
                CYAN
        );

        choose.setTextSize(
                14
        );

        choose.setAllCaps(
                false
        );

        choose.setBackgroundResource(
                R.drawable.card_bg
        );

        root.addView(
                choose,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        TextView count =
                label(
                        String.valueOf(
                                value[0]
                        ),
                        CYAN,
                        62,
                        true
                );

        count.setGravity(
                Gravity.CENTER
        );

        root.addView(
                count,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        Button add =
                createActionButton(
                        "سبّح  +1"
                );

        root.addView(
                add,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(62)
                )
        );

        Button reset =
                new Button(this);

        reset.setText(
                "إعادة العداد"
        );

        reset.setTextColor(
                RED
        );

        reset.setTextSize(
                15
        );

        reset.setAllCaps(
                false
        );

        reset.setBackgroundResource(
                R.drawable.card_bg
        );

        LinearLayout.LayoutParams resetParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        resetParams.setMargins(
                0,
                dp(10),
                0,
                0
        );

        root.addView(
                reset,
                resetParams
        );

        TextView back =
                label(
                        "‹  العودة للرئيسية",
                        MUTED,
                        15,
                        true
                );

        back.setGravity(
                Gravity.CENTER
        );

        back.setPadding(
                0,
                dp(12),
                0,
                dp(6)
        );

        root.addView(
                back
        );

        choose.setOnClickListener(
                v -> {

                    new AlertDialog.Builder(this)
                            .setTitle(
                                    "اختر الذكر"
                            )
                            .setSingleChoiceItems(
                                    names,
                                    selected[0],
                                    (dialog, which) -> {

                                        selected[0] = which;

                                        selectedText.setText(
                                                names[which]
                                        );

                                        dialog.dismiss();
                                    }
                            )
                            .show();
                }
        );

        add.setOnClickListener(
                v -> {

                    value[0]++;

                    count.setText(
                            String.valueOf(
                                    value[0]
                            )
                    );

                    prefs.edit()
                            .putInt(
                                    "tasbeeh",
                                    value[0]
                            )
                            .apply();
                }
        );

        reset.setOnClickListener(
                v -> {

                    value[0] = 0;

                    count.setText(
                            "0"
                    );

                    prefs.edit()
                            .putInt(
                                    "tasbeeh",
                                    0
                            )
                            .apply();
                }
        );

        back.setOnClickListener(
                v -> showHome()
        );

        setContentView(
                root
        );
    }

    /*
     * الإعدادات
     */
    private void settings() {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setBackgroundColor(
                BG
        );

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(18),
                dp(14),
                dp(18),
                dp(18)
        );

        root.setBackgroundColor(
                BG
        );

        scroll.addView(
                root
        );

        TextView title =
                label(
                        "⚙️  إعدادات التذكيرات",
                        GREEN,
                        26,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        TextView description =
                label(
                        "يمكنك تشغيل أو إيقاف كل نوع وتحديد ساعة ودقيقة مستقلة له.",
                        MUTED,
                        14,
                        false
                );

        description.setGravity(
                Gravity.CENTER
        );

        description.setPadding(
                0,
                0,
                0,
                dp(12)
        );

        root.addView(
                description
        );

        LinearLayout generalCard =
                reminderCard(
                        "🔔  التذكير العام",
                        ReminderScheduler.TYPE_GENERAL
                );

        root.addView(
                generalCard
        );

        LinearLayout morningCard =
                reminderCard(
                        "🌅  أذكار الصباح",
                        ReminderScheduler.TYPE_MORNING
                );

        LinearLayout.LayoutParams morningParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        morningParams.setMargins(
                0,
                dp(10),
                0,
                0
        );

        root.addView(
                morningCard,
                morningParams
        );

        LinearLayout eveningCard =
                reminderCard(
                        "🌙  أذكار المساء",
                        ReminderScheduler.TYPE_EVENING
                );

        LinearLayout.LayoutParams eveningParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        eveningParams.setMargins(
                0,
                dp(10),
                0,
                0
        );

        root.addView(
                eveningCard,
                eveningParams
        );

        TextView locationTitle =
                label(
                        "📍 الموقع ومواقيت الصلاة",
                        CYAN,
                        18,
                        true
                );

        locationTitle.setGravity(
                Gravity.CENTER
        );

        locationTitle.setPadding(
                0,
                dp(18),
                0,
                dp(8)
        );

        root.addView(
                locationTitle
        );

        Button locationButton =
                createActionButton(
                        "📍 تحديث الموقع"
                );

        root.addView(
                locationButton,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(54)
                )
        );

        locationButton.setOnClickListener(
                v -> requestLocationPermissionIfNeeded()
        );

        TextView info =
                label(
                        "مواقيت الصلاة تُحسب محليًا من إحداثيات الهاتف دون API خارجي. طريقة الحساب: زاوية الفجر 18° والعشاء 17°.",
                        MUTED,
                        11,
                        false
                );

        info.setGravity(
                Gravity.CENTER
        );

        info.setPadding(
                dp(8),
                dp(12),
                dp(8),
                dp(10)
        );

        root.addView(
                info
        );

        Button back =
                createActionButton(
                        "‹  العودة للرئيسية"
                );

        root.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(54)
                )
        );

        back.setOnClickListener(
                v -> showHome()
        );

        setContentView(
                scroll
        );
    }

    /*
     * بطاقة إعداد التذكير
     */
    private LinearLayout reminderCard(
            String title,
            String type
    ) {

        boolean general =
                ReminderScheduler.TYPE_GENERAL.equals(
                        type
                );

        boolean morning =
                ReminderScheduler.TYPE_MORNING.equals(
                        type
                );

        String enabledKey;
        String hourKey;
        String minuteKey;
        int defaultHour;

        if (general) {

            enabledKey = GENERAL_ENABLED;
            hourKey = GENERAL_HOUR;
            minuteKey = GENERAL_MINUTE;
            defaultHour = 12;

        } else if (morning) {

            enabledKey = MORNING_ENABLED;
            hourKey = MORNING_HOUR;
            minuteKey = MORNING_MINUTE;
            defaultHour = 6;

        } else {

            enabledKey = EVENING_ENABLED;
            hourKey = EVENING_HOUR;
            minuteKey = EVENING_MINUTE;
            defaultHour = 17;
        }

        boolean enabled =
                prefs.getBoolean(
                        enabledKey,
                        true
                );

        int hour =
                prefs.getInt(
                        hourKey,
                        defaultHour
                );

        int minute =
                prefs.getInt(
                        minuteKey,
                        0
                );

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        card.setBackgroundResource(
                R.drawable.card_bg
        );

        TextView name =
                label(
                        title,
                        WHITE,
                        19,
                        true
                );

        name.setGravity(
                Gravity.CENTER
        );

        card.addView(
                name
        );

        TextView time =
                label(
                        formatTime(
                                hour,
                                minute
                        ),
                        CYAN,
                        25,
                        true
                );

        time.setGravity(
                Gravity.CENTER
        );

        time.setPadding(
                0,
                dp(8),
                0,
                dp(8)
        );

        card.addView(
                time
        );

        TextView state =
                label(
                        enabled
                                ? "🟢 مفعّل يوميًا"
                                : "⚪ متوقف",
                        enabled
                                ? GREEN
                                : MUTED,
                        14,
                        true
                );

        state.setGravity(
                Gravity.CENTER
        );

        card.addView(
                state
        );

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setGravity(
                Gravity.CENTER
        );

        Button toggle =
                new Button(this);

        toggle.setText(
                enabled
                        ? "إيقاف"
                        : "تشغيل"
        );

        toggle.setTextSize(
                14
        );

        toggle.setAllCaps(
                false
        );

        toggle.setTextColor(
                enabled
                        ? RED
                        : GREEN
        );

        toggle.setBackgroundResource(
                R.drawable.card_bg
        );

        Button change =
                createActionButton(
                        "تغيير الوقت"
                );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                );

        buttonParams.setMargins(
                dp(3),
                0,
                dp(3),
                0
        );

        buttons.addView(
                toggle,
                buttonParams
        );

        buttons.addView(
                change,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        card.addView(
                buttons
        );

        toggle.setOnClickListener(
                v -> {

                    boolean current =
                            prefs.getBoolean(
                                    enabledKey,
                                    true
                            );

                    boolean newValue =
                            !current;

                    prefs.edit()
                            .putBoolean(
                                    enabledKey,
                                    newValue
                            )
                            .apply();

                    if (newValue) {

                        ReminderScheduler.schedule(
                                this,
                                type,
                                prefs.getInt(
                                        hourKey,
                                        defaultHour
                                ),
                                prefs.getInt(
                                        minuteKey,
                                        0
                                )
                        );

                    } else {

                        if (general) {

                            ReminderScheduler.cancelGeneral(
                                    this
                            );

                        } else if (morning) {

                            ReminderScheduler.cancelMorning(
                                    this
                            );

                        } else {

                            ReminderScheduler.cancelEvening(
                                    this
                            );
                        }
                    }

                    settings();
                }
        );

        change.setOnClickListener(
                v -> chooseReminderTime(
                        type
                )
        );

        return card;
    }

    /*
     * تغيير وقت التذكير
     */
    private void chooseReminderTime(
            String type
    ) {

        boolean general =
                ReminderScheduler.TYPE_GENERAL.equals(
                        type
                );

        boolean morning =
                ReminderScheduler.TYPE_MORNING.equals(
                        type
                );

        String hourKey;
        String minuteKey;
        String enabledKey;
        int defaultHour;
        String title;

        if (general) {

            hourKey = GENERAL_HOUR;
            minuteKey = GENERAL_MINUTE;
            enabledKey = GENERAL_ENABLED;
            defaultHour = 12;
            title = "وقت التذكير العام";

        } else if (morning) {

            hourKey = MORNING_HOUR;
            minuteKey = MORNING_MINUTE;
            enabledKey = MORNING_ENABLED;
            defaultHour = 6;
            title = "وقت أذكار الصباح";

        } else {

            hourKey = EVENING_HOUR;
            minuteKey = EVENING_MINUTE;
            enabledKey = EVENING_ENABLED;
            defaultHour = 17;
            title = "وقت أذكار المساء";
        }

        int hour =
                prefs.getInt(
                        hourKey,
                        defaultHour
                );

        int minute =
                prefs.getInt(
                        minuteKey,
                        0
                );

        TimePicker picker =
                new TimePicker(this);

        picker.setIs24HourView(
                false
        );

        picker.setHour(
                hour
        );

        picker.setMinute(
                minute
        );

        new AlertDialog.Builder(this)
                .setTitle(
                        title
                )
                .setView(
                        picker
                )
                .setPositiveButton(
                        "حفظ",
                        (dialog, which) -> {

                            int selectedHour =
                                    picker.getHour();

                            int selectedMinute =
                                    picker.getMinute();

                            prefs.edit()
                                    .putInt(
                                            hourKey,
                                            selectedHour
                                    )
                                    .putInt(
                                            minuteKey,
                                            selectedMinute
                                    )
                                    .apply();

                            boolean enabled =
                                    prefs.getBoolean(
                                            enabledKey,
                                            true
                                    );

                            if (enabled) {

                                ReminderScheduler.schedule(
                                        this,
                                        type,
                                        selectedHour,
                                        selectedMinute
                                );
                            }

                            settings();

                            Toast.makeText(
                                    this,
                                    "تم حفظ وقت التذكير اليومي",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .show();
    }

    /*
     * مواقيت الصلاة
     */
    private void prayerTimes() {

        /*
         * نحاول أولًا الحصول على آخر موقع معروف.
         * هذا مهم خصوصًا إذا كان المستخدم قد منح
         * الإذن سابقًا لكن لم تُحفظ الإحداثيات.
         */
        loadLastKnownLocation();

        double latitude =
                prefs.getFloat(
                        LATITUDE,
                        Float.NaN
                );

        double longitude =
                prefs.getFloat(
                        LONGITUDE,
                        Float.NaN
                );

        if (Double.isNaN(latitude) ||
                Double.isNaN(longitude)) {

            requestLocationPermissionIfNeeded();

            new AlertDialog.Builder(this)
                    .setTitle(
                            "📍 الموقع مطلوب"
                    )
                    .setMessage(
                            "يحتاج التطبيق إلى موقع الهاتف لحساب مواقيت الصلاة محليًا. اسمح بالوصول إلى الموقع ثم افتح مواقيت الصلاة مرة أخرى."
                    )
                    .setPositiveButton(
                            "السماح بالموقع",
                            (dialog, which) ->
                                    requestLocationPermissionIfNeeded()
                    )
                    .setNegativeButton(
                            "إلغاء",
                            null
                    )
                    .show();

            return;
        }

        PrayerTimes times =
                calculatePrayerTimes(
                        latitude,
                        longitude,
                        Calendar.getInstance()
                );

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                dp(18),
                dp(14),
                dp(18),
                dp(18)
        );

        root.setBackgroundColor(
                BG
        );

        TextView title =
                label(
                        "🕌  مواقيت الصلاة",
                        GREEN,
                        27,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        TextView location =
                label(
                        String.format(
                                Locale.US,
                                "📍 %.5f , %.5f",
                                latitude,
                                longitude
                        ),
                        MUTED,
                        11,
                        false
                );

        location.setGravity(
                Gravity.CENTER
        );

        root.addView(
                location
        );

        addPrayerRow(
                root,
                "🌅 الفجر",
                times.fajr
        );

        addPrayerRow(
                root,
                "☀️ الشروق",
                times.sunrise
        );

        addPrayerRow(
                root,
                "☀️ الظهر",
                times.dhuhr
        );

        addPrayerRow(
                root,
                "🌤️ العصر",
                times.asr
        );

        addPrayerRow(
                root,
                "🌇 المغرب",
                times.maghrib
        );

        addPrayerRow(
                root,
                "🌙 العشاء",
                times.isha
        );

        TextView method =
                label(
                        "حساب محلي • الفجر 18° • العشاء 17° • العصر بمعيار الظل 1",
                        MUTED,
                        11,
                        false
                );

        method.setGravity(
                Gravity.CENTER
        );

        method.setPadding(
                0,
                dp(12),
                0,
                dp(8)
        );

        root.addView(
                method
        );

        Button refresh =
                createActionButton(
                        "📍 تحديث الموقع"
                );

        root.addView(
                refresh,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                )
        );

        refresh.setOnClickListener(
                v -> {

                    requestLocationPermissionIfNeeded();

                    loadLastKnownLocation();

                    Toast.makeText(
                            this,
                            "تم تحديث الموقع إن كان متاحًا",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        Button back =
                createActionButton(
                        "‹  العودة للرئيسية"
                );

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(54)
                );

        backParams.setMargins(
                0,
                dp(10),
                0,
                0
        );

        root.addView(
                back,
                backParams
        );

        back.setOnClickListener(
                v -> showHome()
        );

        setContentView(
                root
        );
    }

    /*
     * صف مواقيت الصلاة
     */
    private void addPrayerRow(
            LinearLayout root,
            String name,
            String time
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(14),
                dp(4),
                dp(14),
                dp(4)
        );

        row.setBackgroundResource(
                R.drawable.card_bg
        );

        TextView nameText =
                label(
                        name,
                        WHITE,
                        17,
                        true
                );

        nameText.setGravity(
                Gravity.CENTER
        );

        row.addView(
                nameText,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        TextView timeText =
                label(
                        time,
                        CYAN,
                        19,
                        true
                );

        timeText.setGravity(
                Gravity.CENTER
        );

        row.addView(
                timeText,
                new LinearLayout.LayoutParams(
                        dp(110),
                        dp(52)
                )
        );

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                );

        rowParams.setMargins(
                0,
                dp(4),
                0,
                dp(4)
        );

        root.addView(
                row,
                rowParams
        );
    }

    /*
     * قراءة آخر موقع معروف
     */
    private void loadLastKnownLocation() {

        if (Build.VERSION.SDK_INT >= 23) {

            boolean fine =
                    checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            boolean coarse =
                    checkSelfPermission(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            if (!fine && !coarse) {
                return;
            }
        }

        LocationManager manager =
                (LocationManager)
                        getSystemService(
                                LOCATION_SERVICE
                        );

        if (manager == null) {
            return;
        }

        Location best =
                null;

        try {

            Location gps =
                    manager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER
                    );

            Location network =
                    manager.getLastKnownLocation(
                            LocationManager.NETWORK_PROVIDER
                    );

            best =
                    chooseBetterLocation(
                            gps,
                            network
                    );

        } catch (SecurityException ignored) {
        }

        if (best != null) {

            saveLocation(
                    best.getLatitude(),
                    best.getLongitude()
            );
        }
    }

    private Location chooseBetterLocation(
            Location a,
            Location b
    ) {

        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        if (a.getTime() >= b.getTime()) {
            return a;
        }

        return b;
    }

    private void saveLocation(
            double latitude,
            double longitude
    ) {

        prefs.edit()
                .putFloat(
                        LATITUDE,
                        (float) latitude
                )
                .putFloat(
                        LONGITUDE,
                        (float) longitude
                )
                .apply();
    }

    /*
     * ============================================================
     * حساب مواقيت الصلاة محليًا
     * ============================================================
     *
     * الحساب يعتمد على:
     * - خط العرض
     * - خط الطول
     * - اليوم الحالي
     * - المنطقة الزمنية المحلية للهاتف
     *
     * الزوايا:
     * الفجر  = 18°
     * العشاء = 17°
     * الشروق/الغروب = -0.833°
     * العصر = معيار الظل 1
     */
    private PrayerTimes calculatePrayerTimes(
            double latitude,
            double longitude,
            Calendar date
    ) {

        int dayOfYear =
                date.get(
                        Calendar.DAY_OF_YEAR
                );

        /*
         * زاوية اليوم الشمسية
         */
        double gamma =
                2.0 * Math.PI / 365.0 *
                        (dayOfYear - 1);

        /*
         * معادلة الزمن بالدقائق
         */
        double equationOfTime =
                229.18 *
                        (
                                0.000075
                                        +
                                0.001868 *
                                        Math.cos(gamma)
                                        -
                                0.032077 *
                                        Math.sin(gamma)
                                        -
                                0.014615 *
                                        Math.cos(2.0 * gamma)
                                        -
                                0.040849 *
                                        Math.sin(2.0 * gamma)
                        );

        /*
         * ميل الشمس بالراديان
         */
        double declination =
                0.006918
                        -
                0.399912 *
                        Math.cos(gamma)
                        +
                0.070257 *
                        Math.sin(gamma)
                        -
                0.006758 *
                        Math.cos(2.0 * gamma)
                        +
                0.000907 *
                        Math.sin(2.0 * gamma)
                        -
                0.002697 *
                        Math.cos(3.0 * gamma)
                        +
                0.001480 *
                        Math.sin(3.0 * gamma);

        /*
         * المنطقة الزمنية المحلية للهاتف
         */
        double timezone =
                TimeZoneHolder.offsetHours();

        /*
         * الظهر الشمسي
         */
        double solarNoon =
                720.0
                        -
                (4.0 * longitude)
                        -
                equationOfTime
                        +
                (60.0 * timezone);

        /*
         * زاوية الشروق والغروب
         *
         * -0.833 درجة تشمل تقريبًا:
         * انكسار الغلاف الجوي + نصف قطر قرص الشمس.
         */
        double sunriseSunsetAngle =
                -0.833;

        double sunriseHourAngle =
                hourAngle(
                        latitude,
                        declination,
                        sunriseSunsetAngle
                );

        /*
         * الشروق
         */
        double sunriseTime =
                solarNoon
                        -
                (4.0 * sunriseHourAngle);

        /*
         * الغروب
         */
        double sunsetTime =
                solarNoon
                        +
                (4.0 * sunriseHourAngle);

        /*
         * الفجر
         * زاوية الشمس -18°
         */
        double fajrHourAngle =
                hourAngle(
                        latitude,
                        declination,
                        -18.0
                );

        double fajrTime =
                solarNoon
                        -
                (4.0 * fajrHourAngle);

        /*
         * العشاء
         * زاوية الشمس -17°
         */
        double ishaHourAngle =
                hourAngle(
                        latitude,
                        declination,
                        -17.0
                );

        double ishaTime =
                solarNoon
                        +
                (4.0 * ishaHourAngle);

        /*
         * العصر
         *
         * معيار الظل 1:
         * طول ظل الجسم = طول الجسم نفسه
         */
        double asrTime =
                calculateAsr(
                        solarNoon,
                        latitude,
                        declination,
                        1
                );

        /*
         * إنشاء النتيجة
         */
        PrayerTimes result =
                new PrayerTimes();

        result.fajr =
                formatPrayerTime(
                        fajrTime
                );

        result.sunrise =
                formatPrayerTime(
                        sunriseTime
                );

        result.dhuhr =
                formatPrayerTime(
                        solarNoon
                );

        result.asr =
                formatPrayerTime(
                        asrTime
                );

        result.maghrib =
                formatPrayerTime(
                        sunsetTime
                );

        result.isha =
                formatPrayerTime(
                        ishaTime
                );

        return result;
    }

    /*
     * حساب وقت الشمس عند زاوية معينة
     */
    private double solarTime(
            double solarNoon,
            double latitude,
            double declination,
            double solarAltitude
    ) {

        double h =
                hourAngle(
                        latitude,
                        declination,
                        solarAltitude
                );

        return solarNoon +
                (4.0 * h);
    }

    /*
     * حساب زاوية الساعة
     *
     * النتيجة بالدرجات.
     * كل درجة تعادل 4 دقائق.
     */
    private double hourAngle(
            double latitude,
            double declination,
            double solarAltitude
    ) {

        double latRad =
                Math.toRadians(
                        latitude
                );

        double altitudeRad =
                Math.toRadians(
                        solarAltitude
                );

        double sinLat =
                Math.sin(
                        latRad
                );

        double cosLat =
                Math.cos(
                        latRad
                );

        double sinDeclination =
                Math.sin(
                        declination
                );

        double cosDeclination =
                Math.cos(
                        declination
                );

        double sinAltitude =
                Math.sin(
                        altitudeRad
                );

        /*
         * معادلة زاوية الساعة:
         *
         * cos(H) =
         * (sin(h) - sin(phi)sin(delta))
         * /
         * (cos(phi)cos(delta))
         */
        double denominator =
                cosLat *
                        cosDeclination;

        /*
         * حماية من القسمة على صفر
         */
        if (Math.abs(denominator) < 0.000001) {

            return 0.0;
        }

        double cosH =
                (
                        sinAltitude
                                -
                        (sinLat *
                                sinDeclination)
                )
                        /
                        denominator;

        /*
         * منع أخطاء acos الناتجة عن
         * قيم بسيطة خارج النطاق بسبب التقريب.
         */
        if (cosH > 1.0) {

            cosH = 1.0;

        } else if (cosH < -1.0) {

            cosH = -1.0;
        }

        return Math.toDegrees(
                Math.acos(
                        cosH
                )
        );
    }

    /*
     * حساب العصر
     *
     * shadowFactor = 1
     * يعني معيار الظل الأول.
     */
    private double calculateAsr(
            double solarNoon,
            double latitude,
            double declination,
            int shadowFactor
    ) {

        double latRad =
                Math.toRadians(
                        latitude
                );

        /*
         * زاوية ارتفاع الشمس المطلوبة للعصر.
         *
         * معيار الظل:
         * 1 = ظل الجسم يساوي طول الجسم
         */
        double altitude =
                -Math.toDegrees(
                        Math.atan(
                                1.0 /
                                        (
                                                shadowFactor
                                                        +
                                                Math.tan(
                                                        Math.abs(
                                                                latRad
                                                        )
                                                )
                                        )
                        )
                );

        double h =
                hourAngle(
                        latitude,
                        declination,
                        altitude
                );

        return solarNoon +
                (4.0 * h);
    }

    /*
     * تحويل الدقائق الفلكية إلى وقت
     */
    private String formatPrayerTime(
            double minutes
    ) {

        if (Double.isNaN(minutes) ||
                Double.isInfinite(minutes)) {

            return "--:--";
        }

        /*
         * تطبيع الوقت إلى 24 ساعة.
         */
        while (minutes < 0.0) {

            minutes += 1440.0;
        }

        while (minutes >= 1440.0) {

            minutes -= 1440.0;
        }

        int hour =
                (int) (
                        minutes / 60.0
                );

        int minute =
                (int) Math.round(
                        minutes % 60.0
                );

        /*
         * معالجة حالة التقريب:
         * 12:59.8 -> 13:00
         */
        if (minute >= 60) {

            minute = 0;
            hour++;
        }

        hour %= 24;

        return formatTime(
                hour,
                minute
        );
    }

    /*
     * معلومات مواقيت الصلاة
     */
    private static class PrayerTimes {

        String fajr;
        String sunrise;
        String dhuhr;
        String asr;
        String maghrib;
        String isha;
    }

    /*
     * المنطقة الزمنية المحلية
     */
    private static class TimeZoneHolder {

        static double offsetHours() {

            return TimeZoneOffset.get();
        }
    }

    /*
     * الحصول على فرق المنطقة الزمنية
     * من إعدادات الهاتف.
     */
    private static class TimeZoneOffset {

        static double get() {

            java.util.TimeZone zone =
                    java.util.TimeZone.getDefault();

            long now =
                    System.currentTimeMillis();

            int offset =
                    zone.getOffset(
                            now
                    );

            return offset /
                    3600000.0;
        }
    }

    /*
     * حول وذكر
     */
    private void about() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                dp(24),
                dp(18),
                dp(24),
                dp(18)
        );

        root.setBackgroundColor(
                BG
        );

        ImageView icon =
                new ImageView(this);

        icon.setImageResource(
                R.drawable.icon_source
        );

        root.addView(
                icon,
                new LinearLayout.LayoutParams(
                        dp(90),
                        dp(90)
                )
        );

        TextView title =
                label(
                        "وذكر",
                        GREEN,
                        31,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title
        );

        TextView version =
                label(
                        "الإصدار 2.1.0",
                        CYAN,
                        14,
                        false
                );

        version.setGravity(
                Gravity.CENTER
        );

        version.setPadding(
                0,
                dp(4),
                0,
                dp(18)
        );

        root.addView(
                version
        );

        TextView developer =
                label(
                        "المطور\nصالح الخليفي",
                        WHITE,
                        18,
                        true
                );

        developer.setGravity(
                Gravity.CENTER
        );

        root.addView(
                developer
        );

        TextView telegram =
                label(
                        "✈️  @iSx3i",
                        CYAN,
                        18,
                        true
                );

        telegram.setGravity(
                Gravity.CENTER
        );

        telegram.setPadding(
                0,
                dp(18),
                0,
                dp(18)
        );

        root.addView(
                telegram
        );

        telegram.setOnClickListener(
                v -> {

                    try {

                        Intent intent =
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                                "https://t.me/iSx3i"
                                        )
                                );

                        startActivity(
                                intent
                        );

                    } catch (Exception ignored) {
                    }
                }
        );

        Button back =
                createActionButton(
                        "‹  العودة للرئيسية"
                );

        root.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56)
                )
        );

        back.setOnClickListener(
                v -> showHome()
        );

        setContentView(
                root
        );
    }

    @Override
    public void onBackPressed() {

        if (currentData != null) {

            showHome();

            return;
        }

        showHome();
    }

    /*
     * تسجيل ذكر مكتمل
     */
    public static void recordDhikr(
            Context context
    ) {

        SharedPreferences p =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        String today =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                ).format(
                        new Date()
                );

        String savedDate =
                p.getString(
                        "date",
                        ""
                );

        int count =
                p.getInt(
                        "count",
                        0
                );

        if (!today.equals(savedDate)) {
            count = 0;
        }

        p.edit()
                .putString(
                        "date",
                        today
                )
                .putInt(
                        "count",
                        count + 1
                )
                .apply();
    }

    /*
     * إنشاء زر موحد
     */
    private Button createActionButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(
                text
        );

        button.setTextColor(
                BG
        );

        button.setTextSize(
                15
        );

        button.setAllCaps(
                false
        );

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setBackgroundResource(
                R.drawable.neon_button
        );

        return button;
    }

    /*
     * تقدم التكرار
     */
    private String progress(
            int current,
            int target
    ) {

        return "التكرار  " +
                current +
                " / " +
                target;
    }

    /*
     * تنسيق الوقت
     */
    private String formatTime(
            int hour,
            int minute
    ) {

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                hour
        );

        calendar.set(
                Calendar.MINUTE,
                minute
        );

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "h:mm a",
                        Locale.getDefault()
                );

        String result =
                format.format(
                        calendar.getTime()
                );

        return result
                .replace(
                        "AM",
                        "ص"
                )
                .replace(
                        "PM",
                        "م"
                );
    }

    /*
     * TextView موحد
     */
    private TextView label(
            String text,
            int color,
            float size,
            boolean bold
    ) {

        TextView t =
                new TextView(this);

        t.setText(
                text
        );

        t.setTextColor(
                color
        );

        t.setTextSize(
                size
        );

        t.setGravity(
                Gravity.CENTER_VERTICAL
        );

        t.setTypeface(
                Typeface.DEFAULT,
                bold
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        t.setIncludeFontPadding(
                true
        );

        return t;
    }

    /*
     * dp إلى px
     */
    private int dp(
            int value
    ) {

        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    /*
     * تاريخ اليوم
     */
    private String today() {

        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(
                new Date()
        );
    }
            }
