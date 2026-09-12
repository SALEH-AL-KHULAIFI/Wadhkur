package com.saleh.wadhkur;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(4, 8, 15);
    private static final int CARD = Color.rgb(9, 20, 31);
    private static final int GREEN = Color.rgb(57, 255, 143);
    private static final int CYAN = Color.rgb(55, 220, 255);
    private static final int PURPLE = Color.rgb(169, 92, 255);
    private static final int WHITE = Color.rgb(239, 250, 245);
    private static final int MUTED = Color.rgb(150, 170, 182);
    private static final int RED = Color.rgb(255, 85, 105);

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

    private Handler countdownHandler = new Handler(Looper.getMainLooper());
    private TextView ramadanCountdownText;

    private final Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            if (ramadanCountdownText != null) {
                updateRamadanCountdown();
                countdownHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        showHome();

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    44
            );
        }
    }

    @Override
    protected void onDestroy() {
        countdownHandler.removeCallbacks(countdownRunnable);
        super.onDestroy();
    }

    private void showHome() {

        setContentView(R.layout.activity_main);

        countText = findViewById(R.id.countText);
        statusText = findViewById(R.id.statusText);

        findViewById(R.id.morningBtn).setOnClickListener(
                v -> section("🌅 أذكار الصباح", DhikrData.MORNING)
        );

        findViewById(R.id.eveningBtn).setOnClickListener(
                v -> section("🌆 أذكار المساء", DhikrData.EVENING)
        );

        findViewById(R.id.duasBtn).setOnClickListener(
                v -> section("🤲 الأدعية", DhikrData.DUAS)
        );

        findViewById(R.id.surasBtn).setOnClickListener(
                v -> section("📖 السور والآيات", DhikrData.SURAS)
        );

        findViewById(R.id.tasbeehBtn).setOnClickListener(
                v -> tasbeeh()
        );

        findViewById(R.id.settingsBtn).setOnClickListener(
                v -> settings()
        );

        findViewById(R.id.aboutBtn).setOnClickListener(
                v -> about()
        );

        refresh();

        addRamadanCountdown();

        addExitButton();
    }

    private void addRamadanCountdown() {

        try {

            ScrollView scroll = findViewById(android.R.id.content)
                    .findViewById(android.R.id.content);

        } catch (Exception ignored) {
        }

        View content = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        if (!(content instanceof ScrollView)) {
            return;
        }

        ScrollView scrollView = (ScrollView) content;

        if (!(scrollView.getChildAt(0) instanceof LinearLayout)) {
            return;
        }

        LinearLayout root = (LinearLayout) scrollView.getChildAt(0);

        LinearLayout ramadanCard = new LinearLayout(this);
        ramadanCard.setOrientation(LinearLayout.VERTICAL);
        ramadanCard.setGravity(Gravity.CENTER);
        ramadanCard.setPadding(
                dp(16),
                dp(13),
                dp(16),
                dp(13)
        );
        ramadanCard.setBackgroundColor(Color.rgb(7, 24, 30));
        ramadanCard.setElevation(dp(5));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        cardParams.setMargins(
                0,
                dp(12),
                0,
                dp(2)
        );

        ramadanCard.setLayoutParams(cardParams);

        TextView title = label(
                "🌙  العد التنازلي لرمضان 1448",
                GREEN,
                17,
                true
        );

        title.setGravity(Gravity.CENTER);

        ramadanCard.addView(title);

        ramadanCountdownText = label(
                "جاري الحساب...",
                CYAN,
                22,
                true
        );

        ramadanCountdownText.setGravity(Gravity.CENTER);
        ramadanCountdownText.setPadding(
                0,
                dp(6),
                0,
                dp(3)
        );

        ramadanCard.addView(ramadanCountdownText);

        TextView note = label(
                "الموعد المتوقع: 8 فبراير 2027 • قد يختلف حسب رؤية الهلال",
                MUTED,
                11,
                false
        );

        note.setGravity(Gravity.CENTER);

        ramadanCard.addView(note);

        root.addView(ramadanCard, 1);

        updateRamadanCountdown();

        countdownHandler.removeCallbacks(countdownRunnable);
        countdownHandler.postDelayed(countdownRunnable, 1000);
    }

    private void updateRamadanCountdown() {

        if (ramadanCountdownText == null) {
            return;
        }

        Calendar target = Calendar.getInstance();
        target.set(2027, Calendar.FEBRUARY, 8, 0, 0, 0);
        target.set(Calendar.MILLISECOND, 0);

        long difference = target.getTimeInMillis()
                - System.currentTimeMillis();

        if (difference <= 0) {

            ramadanCountdownText.setText(
                    "🌙 رمضان 1448 بدأ — رمضان كريم"
            );

            return;
        }

        long totalSeconds = difference / 1000;

        long days = totalSeconds / 86400;
        totalSeconds %= 86400;

        long hours = totalSeconds / 3600;
        totalSeconds %= 3600;

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        ramadanCountdownText.setText(
                "باقي " +
                        days +
                        " يوم  •  " +
                        String.format(Locale.US, "%02d:%02d:%02d",
                                hours,
                                minutes,
                                seconds)
        );
    }

    private void addExitButton() {

        View content =
                ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        if (!(content instanceof ScrollView)) {
            return;
        }

        ScrollView scrollView = (ScrollView) content;

        if (!(scrollView.getChildAt(0) instanceof LinearLayout)) {
            return;
        }

        LinearLayout root = (LinearLayout) scrollView.getChildAt(0);

        Button exit = new Button(this);

        exit.setText("⏻  خروج من التطبيق");
        exit.setTextColor(RED);
        exit.setTextSize(15);
        exit.setAllCaps(false);
        exit.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        exit.setBackgroundResource(R.drawable.card_bg);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        params.setMargins(
                0,
                dp(8),
                0,
                dp(8)
        );

        root.addView(exit, params);

        exit.setOnClickListener(v -> confirmExit());
    }

    private void confirmExit() {

        new AlertDialog.Builder(this)
                .setTitle("الخروج من وذكر")
                .setMessage("هل تريد إغلاق التطبيق؟")
                .setPositiveButton(
                        "خروج",
                        (dialog, which) -> finishAffinity()
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .show();
    }

    private void refresh() {

        String d = today();

        if (!d.equals(prefs.getString("date", ""))) {

            prefs.edit()
                    .putString("date", d)
                    .putInt("count", 0)
                    .apply();
        }

        countText.setText(
                String.valueOf(
                        prefs.getInt("count", 0)
                )
        );

        boolean enabled =
                prefs.getBoolean("enabled", false);

        long minutes =
                prefs.getLong("minutes", 30);

        if (enabled) {

            statusText.setText(
                    "🟢 نشط الآن • تذكير كل " +
                            minutes +
                            " دقيقة"
            );

            statusText.setTextColor(GREEN);

        } else {

            statusText.setText(
                    "⚪ التذكيرات متوقفة"
            );

            statusText.setTextColor(MUTED);
        }
    }

    /*
     * شاشة الأذكار الجديدة:
     * بطاقة واحدة فقط في كل مرة.
     */
    private void section(
            String title,
            DhikrData.Dhikr[] data
    ) {

        currentTitle = title;
        currentData = data;
        currentIndex = 0;

        currentRepeat = 0;

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

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

        back.setGravity(Gravity.CENTER);

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

        TextView exit =
                label(
                        "×",
                        RED,
                        30,
                        true
                );

        exit.setGravity(Gravity.CENTER);

        exit.setOnClickListener(
                v -> confirmExit()
        );

        header.addView(
                exit,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(58)
                )
        );

        root.addView(header);

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

        root.addView(instruction);

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

        cardScroll.setFillViewport(true);
        cardScroll.setBackgroundColor(BG);

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

        cardScroll.addView(cardHolder);

        cardContainer.addView(cardScroll);

        root.addView(cardContainer);

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
                new Button(this);

        previous.setText("‹  السابق");
        previous.setTextColor(BG);
        previous.setTextSize(15);
        previous.setAllCaps(false);
        previous.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        previous.setBackgroundResource(
                R.drawable.neon_button
        );

        Button next =
                new Button(this);

        next.setText("التالي  ›");
        next.setTextColor(BG);
        next.setTextSize(15);
        next.setAllCaps(false);
        next.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        next.setBackgroundResource(
                R.drawable.neon_button
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

        root.addView(navigation);

        previous.setOnClickListener(v -> {

            if (currentIndex > 0) {

                currentIndex--;
                showCurrentDhikr(cardHolder);

            } else {

                Toast.makeText(
                        this,
                        "هذا أول ذكر",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        next.setOnClickListener(v -> {

            if (currentIndex < currentData.length - 1) {

                currentIndex++;
                showCurrentDhikr(cardHolder);

            } else {

                Toast.makeText(
                        this,
                        "أكملت آخر ذكر في المجموعة",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        /*
         * السحب الأفقي.
         */
        cardContainer.setOnTouchListener(
                (v, event) -> {

                    switch (event.getActionMasked()) {

                        case MotionEvent.ACTION_DOWN:

                            downX = event.getX();
                            downY = event.getY();

                            return false;

                        case MotionEvent.ACTION_UP:

                            float dx =
                                    event.getX() - downX;

                            float dy =
                                    event.getY() - downY;

                            if (Math.abs(dx) > dp(70) &&
                                    Math.abs(dx) > Math.abs(dy) * 1.2f) {

                                if (dx < 0) {

                                    if (currentIndex <
                                            currentData.length - 1) {

                                        currentIndex++;
                                        showCurrentDhikr(cardHolder);
                                    }

                                } else {

                                    if (currentIndex > 0) {

                                        currentIndex--;
                                        showCurrentDhikr(cardHolder);
                                    }
                                }

                                return true;
                            }

                            return false;
                    }

                    return false;
                }
        );

        setContentView(root);

        showCurrentDhikr(cardHolder);
    }

    private void showCurrentDhikr(
            LinearLayout holder
    ) {

        holder.removeAllViews();

        currentRepeat = 0;

        DhikrData.Dhikr item =
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
                Gravity.CENTER_HORIZONTAL
        );

        card.setPadding(
                dp(20),
                dp(22),
                dp(20),
                dp(20)
        );

        card.setBackgroundResource(
                R.drawable.card_bg
        );

        card.setElevation(
                dp(8)
        );

        holder.addView(
                card,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        dhikrTitleText =
                label(
                        item.title,
                        GREEN,
                        24,
                        true
                );

        dhikrTitleText.setGravity(
                Gravity.CENTER
        );

        card.addView(
                dhikrTitleText,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        dhikrBodyText =
                label(
                        item.text,
                        WHITE,
                        21,
                        false
                );

        dhikrBodyText.setGravity(
                Gravity.CENTER
        );

        dhikrBodyText.setLineSpacing(
                dp(5),
                1.18f
        );

        dhikrBodyText.setPadding(
                0,
                dp(24),
                0,
                dp(20)
        );

        card.addView(
                dhikrBodyText,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        dhikrSourceText =
                label(
                        "المصدر: " + item.source,
                        CYAN,
                        12,
                        false
                );

        dhikrSourceText.setGravity(
                Gravity.CENTER
        );

        card.addView(
                dhikrSourceText,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        dhikrCounterText =
                label(
                        repeatText(0, item.count),
                        PURPLE,
                        17,
                        true
                );

        dhikrCounterText.setGravity(
                Gravity.CENTER
        );

        dhikrCounterText.setPadding(
                0,
                dp(20),
                0,
                dp(12)
        );

        card.addView(
                dhikrCounterText,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        dhikrActionButton =
                new Button(this);

        dhikrActionButton.setText(
                "📿  اضغط للذكر"
        );

        dhikrActionButton.setTextColor(BG);
        dhikrActionButton.setTextSize(17);
        dhikrActionButton.setAllCaps(false);
        dhikrActionButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        dhikrActionButton.setBackgroundResource(
                R.drawable.neon_button
        );

        card.addView(
                dhikrActionButton,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        dhikrActionButton.setOnClickListener(
                v -> {

                    if (currentRepeat >= item.count) {
                        return;
                    }

                    currentRepeat++;

                    recordDhikr(this);

                    dhikrCounterText.setText(
                            repeatText(
                                    currentRepeat,
                                    item.count
                            )
                    );

                    if (currentRepeat >= item.count) {

                        dhikrActionButton.setText(
                                "✓  اكتمل الذكر"
                        );

                        dhikrActionButton.setTextColor(
                                WHITE
                        );

                        dhikrActionButton.setBackgroundColor(
                                Color.rgb(18, 105, 68)
                        );

                        dhikrActionButton.setEnabled(
                                false
                        );
                    }
                }
        );

        /*
         * إعادة التمرير إلى الأعلى عند تغيير الذكر.
         */
        holder.post(() -> {

            View parent = holder.getParent();

            if (parent instanceof ScrollView) {
                ((ScrollView) parent).scrollTo(0, 0);
            }
        });
    }

    private String repeatText(
            int current,
            int total
    ) {

        if (total <= 0) {
            return "✓";
        }

        StringBuilder dots =
                new StringBuilder();

        int visible =
                Math.min(total, 20);

        for (int i = 0; i < visible; i++) {

            dots.append(
                    i < current ? "● " : "○ "
            );
        }

        if (total > 20) {
            dots.append("...");
        }

        if (current >= total) {

            return dots.toString() +
                    "  ✓ مكتمل";
        }

        return dots.toString() +
                "\n" +
                current +
                " / " +
                total;
    }

    private void tasbeeh() {

        final int[] q = {0};

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        box.setPadding(
                dp(24),
                dp(12),
                dp(24),
                dp(6)
        );

        box.setBackgroundColor(BG);

        TextView number =
                label(
                        "0",
                        GREEN,
                        54,
                        true
                );

        number.setGravity(
                Gravity.CENTER
        );

        box.addView(
                number,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(100)
                )
        );

        Button action =
                new Button(this);

        action.setText(
                "📿  سبحان الله"
        );

        action.setTextColor(BG);
        action.setTextSize(18);
        action.setAllCaps(false);
        action.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        action.setBackgroundResource(
                R.drawable.neon_button
        );

        action.setOnClickListener(
                v -> {

                    q[0]++;

                    number.setText(
                            String.valueOf(q[0])
                    );

                    recordDhikr(this);
                }
        );

        box.addView(
                action,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                )
        );

        new AlertDialog.Builder(this)
                .setTitle("📿 عداد التسبيح")
                .setView(box)
                .setNegativeButton(
                        "إغلاق",
                        null
                )
                .show();
    }

    private void settings() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(22),
                0,
                dp(22),
                0
        );

        Switch enabled =
                new Switch(this);

        enabled.setText(
                "تفعيل رفيق الذكر"
        );

        enabled.setTextSize(17);

        enabled.setChecked(
                prefs.getBoolean(
                        "enabled",
                        false
                )
        );

        box.addView(enabled);

        TextView info =
                label(
                        "الفاصل: من 1 إلى 60 دقيقة (الافتراضي 30)",
                        MUTED,
                        14,
                        false
                );

        info.setPadding(
                0,
                dp(12),
                0,
                dp(5)
        );

        box.addView(info);

        EditText minutes =
                new EditText(this);

        minutes.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        minutes.setText(
                String.valueOf(
                        prefs.getLong(
                                "minutes",
                                30
                        )
                )
        );

        minutes.setTextSize(18);

        box.addView(minutes);

        new AlertDialog.Builder(this)
                .setTitle("🔔 رفيق الذكر")
                .setView(box)
                .setPositiveButton(
                        "حفظ",
                        (dialog, which) -> {

                            long value = 30;

                            try {

                                value =
                                        Long.parseLong(
                                                minutes
                                                        .getText()
                                                        .toString()
                                                        .trim()
                                        );

                            } catch (Exception ignored) {
                            }

                            value =
                                    Math.max(
                                            1,
                                            Math.min(
                                                    60,
                                                    value
                                            )
                                    );

                            boolean on =
                                    enabled.isChecked();

                            prefs.edit()
                                    .putBoolean(
                                            "enabled",
                                            on
                                    )
                                    .putLong(
                                            "minutes",
                                            value
                                    )
                                    .apply();

                            if (on) {

                                ReminderScheduler.schedule(
                                        this,
                                        value
                                );

                            } else {

                                ReminderScheduler.cancel(
                                        this
                                );
                            }

                            refresh();
                        }
                )
                .setNegativeButton(
                        "إلغاء",
                        null
                )
                .show();
    }

    private void about() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        box.setPadding(
                dp(22),
                dp(5),
                dp(22),
                dp(5)
        );

        TextView name =
                label(
                        "وذكر 2.1.0",
                        GREEN,
                        27,
                        true
                );

        name.setGravity(
                Gravity.CENTER
        );

        box.addView(name);

        TextView description =
                label(
                        "تطبيق إسلامي للأذكار والأدعية والتسبيح، صُمم ليكون سريعًا وواضحًا ومريحًا للاستخدام اليومي.",
                        WHITE,
                        16,
                        false
                );

        description.setGravity(
                Gravity.CENTER
        );

        description.setPadding(
                0,
                dp(12),
                0,
                dp(12)
        );

        box.addView(description);

        TextView developer =
                label(
                        "المطور: صالح الخليفي",
                        CYAN,
                        16,
                        true
                );

        developer.setGravity(
                Gravity.CENTER
        );

        box.addView(developer);

        TextView telegram =
                label(
                        "Telegram: @iSx3i",
                        GREEN,
                        17,
                        true
                );

        telegram.setGravity(
                Gravity.CENTER
        );

        telegram.setPadding(
                0,
                dp(16),
                0,
                dp(5)
        );

        telegram.setOnClickListener(
                v -> {

                    try {

                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                                "https://t.me/iSx3i"
                                        )
                                )
                        );

                    } catch (Exception ignored) {
                    }
                }
        );

        box.addView(telegram);

        new AlertDialog.Builder(this)
                .setTitle("ℹ️ حول وذكر")
                .setView(box)
                .setPositiveButton(
                        "تم",
                        null
                )
                .show();
    }

    @Override
    public void onBackPressed() {

        if (currentData != null) {

            currentData = null;
            currentIndex = 0;
            currentRepeat = 0;

            showHome();

        } else {

            new AlertDialog.Builder(this)
                    .setTitle("الخروج من وذكر")
                    .setMessage(
                            "هل تريد الخروج من التطبيق؟"
                    )
                    .setPositiveButton(
                            "خروج",
                            (dialog, which) ->
                                    finishAffinity()
                    )
                    .setNegativeButton(
                            "إلغاء",
                            null
                    )
                    .show();
        }
    }

    public static void recordDhikr(Context context) {

        SharedPreferences p =
                context.getSharedPreferences(
                        "settings",
                        MODE_PRIVATE
                );

        String date =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                ).format(new Date());

        if (!date.equals(
                p.getString("date", "")
        )) {

            p.edit()
                    .putString("date", date)
                    .putInt("count", 0)
                    .apply();
        }

        p.edit()
                .putInt(
                        "count",
                        p.getInt("count", 0) + 1
                )
                .apply();
    }

    private TextView label(
            String text,
            int color,
            float size,
            boolean bold
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setGravity(Gravity.RIGHT);

        view.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        if (bold) {

            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private String today() {

        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(new Date());
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density +
                        0.5f
        );
    }
}