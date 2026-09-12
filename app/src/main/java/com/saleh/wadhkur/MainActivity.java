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

    private Handler countdownHandler =
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
    public void onCreate(Bundle b) {

        super.onCreate(b);

        prefs = getSharedPreferences(
                "settings",
                MODE_PRIVATE
        );

        showHome();

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    44
            );
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

        countText = findViewById(
                R.id.countText
        );

        statusText = findViewById(
                R.id.statusText
        );

        findViewById(
                R.id.morningBtn
        ).setOnClickListener(
                v -> section(
                        "🌅 أذكار الصباح",
                        DhikrData.MORNING
                )
        );

        findViewById(
                R.id.eveningBtn
        ).setOnClickListener(
                v -> section(
                        "🌆 أذكار المساء",
                        DhikrData.EVENING
                )
        );

        findViewById(
                R.id.duasBtn
        ).setOnClickListener(
                v -> section(
                        "🤲 الأدعية",
                        DhikrData.DUAS
                )
        );

        findViewById(
                R.id.surasBtn
        ).setOnClickListener(
                v -> section(
                        "📖 السور والآيات",
                        DhikrData.SURAS
                )
        );

        findViewById(
                R.id.tasbeehBtn
        ).setOnClickListener(
                v -> tasbeeh()
        );

        findViewById(
                R.id.settingsBtn
        ).setOnClickListener(
                v -> settings()
        );

        findViewById(
                R.id.aboutBtn
        ).setOnClickListener(
                v -> about()
        );

        refresh();

        addRamadanCountdown();

        addExitButton();
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
                dp(16),
                dp(13),
                dp(16),
                dp(13)
        );

        ramadanCard.setBackgroundResource(
                R.drawable.card_bg
        );

        ramadanCard.setElevation(
                dp(5)
        );

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

        ramadanCard.setLayoutParams(
                cardParams
        );

        TextView title =
                label(
                        "🌙  العد التنازلي لرمضان 1448",
                        GREEN,
                        17,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        ramadanCard.addView(title);

        ramadanCountdownText =
                label(
                        "جاري الحساب...",
                        CYAN,
                        22,
                        true
                );

        ramadanCountdownText.setGravity(
                Gravity.CENTER
        );

        ramadanCountdownText.setPadding(
                0,
                dp(6),
                0,
                dp(3)
        );

        ramadanCard.addView(
                ramadanCountdownText
        );

        TextView note =
                label(
                        "الموعد المتوقع: 8 فبراير 2027 • قد يختلف حسب رؤية الهلال",
                        MUTED,
                        11,
                        false
                );

        note.setGravity(
                Gravity.CENTER
        );

        ramadanCard.addView(note);

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
     * زر الخروج
     */
    private void addExitButton() {

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

        Button exit =
                new Button(this);

        exit.setText(
                "⏻  خروج من التطبيق"
        );

        exit.setTextColor(RED);

        exit.setTextSize(15);

        exit.setAllCaps(false);

        exit.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        exit.setBackgroundResource(
                R.drawable.card_bg
        );

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

        root.addView(
                exit,
                params
        );

        exit.setOnClickListener(
                v -> confirmExit()
        );
    }

    private void confirmExit() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "الخروج من وذكر"
                )
                .setMessage(
                        "هل تريد إغلاق التطبيق؟"
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

    /*
     * تحديث الحصيلة اليومية والتذكيرات
     */
    private void refresh() {

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

        boolean enabled =
                prefs.getBoolean(
                        "enabled",
                        false
                );

        long minutes =
                prefs.getLong(
                        "minutes",
                        30
                );

        if (enabled) {

            statusText.setText(
                    "🟢 نشط الآن • تذكير كل " +
                            minutes +
                            " دقيقة"
            );

            statusText.setTextColor(
                    GREEN
            );

        } else {

            statusText.setText(
                    "⚪ التذكيرات متوقفة"
            );

            statusText.setTextColor(
                    MUTED
            );
        }
    }

    /*
     * شاشة مجموعة الأذكار
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

        /*
         * رأس الصفحة
         */
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

        TextView exit =
                label(
                        "×",
                        RED,
                        30,
                        true
                );

        exit.setGravity(
                Gravity.CENTER
        );

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

        /*
         * التعليمات
         */
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

        /*
         * رقم البطاقة
         */
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

        /*
         * حاوية البطاقة
         */
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

        /*
         * أزرار التنقل
         */
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

        previous.setText(
                "‹  السابق"
        );

        previous.setTextColor(
                BG
        );

        previous.setTextSize(
                15
        );

        previous.setAllCaps(
                false
        );

        previous.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        previous.setBackgroundResource(
                R.drawable.neon_button
        );

        Button next =
                new Button(this);

        next.setText(
                "التالي  ›"
        );

        next.setTextColor(
                BG
        );

        next.setTextSize(
                15
        );

        next.setAllCaps(
                false
        );

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

        root.addView(
                navigation
        );

        /*
         * السابق
         */
        previous.setOnClickListener(v -> {

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
        });

        /*
         * التالي
         */
        next.setOnClickListener(v -> {

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
        });

        /*
         * السحب الأفقي
         */
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

        setContentView(root);

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

        /*
         * تحديث المؤشر
         */
        dhikrPositionText.setText(
                (currentIndex + 1) +
                        " من " +
                        currentData.length
        );

        /*
         * البطاقة
         */
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

        /*
         * عنوان الذكر
         */
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

        /*
         * نص الذكر
         */
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

        /*
         * المصدر
         */
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

        /*
         * عداد التكرار
         *
         * DhikrData يستخدم count وليس repeat.
         */
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

        /*
         * زر التكرار
         */
        dhikrActionButton =
                new Button(this);

        dhikrActionButton.setText(
                currentRepeat >= targetRepeat
                        ? "✓  مكتمل"
                        : "تسبيح / تكرار"
        );

        dhikrActionButton.setTextColor(
                BG
        );

        dhikrActionButton.setTextSize(
                16
        );

        dhikrActionButton.setAllCaps(
                false
        );

        dhikrActionButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        dhikrActionButton.setBackgroundResource(
                R.drawable.neon_button
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

        holder.addView(card);
    }

    /*
     * عداد التسبيح
     */
    private void tasbeeh() {

        final int[] value = {
                prefs.getInt(
                        "tasbeeh",
                        0
                )
        };

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                dp(20),
                dp(15),
                dp(20),
                dp(20)
        );

        root.setBackgroundColor(
                BG
        );

        TextView title =
                label(
                        "📿  عداد التسبيح",
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
                        dp(65)
                )
        );

        TextView count =
                label(
                        String.valueOf(
                                value[0]
                        ),
                        CYAN,
                        64,
                        true
                );

        count.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        root.addView(
                count,
                countParams
        );

        Button add =
                new Button(this);

        add.setText(
                "سبّح  +1"
        );

        add.setTextColor(
                BG
        );

        add.setTextSize(
                20
        );

        add.setAllCaps(
                false
        );

        add.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        add.setBackgroundResource(
                R.drawable.neon_button
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
                dp(15),
                0,
                dp(8)
        );

        root.addView(back);

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

                    count.setText("0");

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

        setContentView(root);
    }

    /*
     * الإعدادات
     */
    private void settings() {

        final EditText input =
                new EditText(this);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        input.setHint(
                "مثال: 30"
        );

        input.setText(
                String.valueOf(
                        prefs.getLong(
                                "minutes",
                                30
                        )
                )
        );

        input.setSelectAllOnFocus(
                true
        );

        new AlertDialog.Builder(this)

                .setTitle(
                        "🔔 إعداد التذكيرات"
                )

                .setMessage(
                        "حدد عدد الدقائق بين كل تذكير."
                )

                .setView(input)

                .setPositiveButton(
                        "حفظ",
                        (dialog, which) -> {

                            try {

                                long minutes =
                                        Long.parseLong(
                                                input.getText()
                                                        .toString()
                                                        .trim()
                                        );

                                if (minutes < 1) {
                                    minutes = 1;
                                }

                                prefs.edit()
                                        .putBoolean(
                                                "enabled",
                                                true
                                        )
                                        .putLong(
                                                "minutes",
                                                minutes
                                        )
                                        .apply();

                                ReminderScheduler.schedule(
                                        this,
                                        minutes
                                );

                                refresh();

                                Toast.makeText(
                                        this,
                                        "تم تفعيل التذكيرات",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } catch (Exception e) {

                                Toast.makeText(
                                        this,
                                        "أدخل رقمًا صحيحًا",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )

                .setNeutralButton(
                        "إيقاف",
                        (dialog, which) -> {

                            prefs.edit()
                                    .putBoolean(
                                            "enabled",
                                            false
                                    )
                                    .apply();

                            ReminderScheduler.cancel(
                                    this
                            );

                            refresh();
                        }
                )

                .setNegativeButton(
                        "إلغاء",
                        null
                )

                .show();
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
                        dp(100),
                        dp(100)
                )
        );

        TextView title =
                label(
                        "وذكر",
                        GREEN,
                        32,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(title);

        TextView version =
                label(
                        "الإصدار 2.0.0",
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
                dp(20)
        );

        root.addView(version);

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
                dp(20),
                0,
                dp(20)
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

                        startActivity(intent);

                    } catch (Exception ignored) {
                    }
                }
        );

        Button back =
                new Button(this);

        back.setText(
                "‹  العودة للرئيسية"
        );

        back.setTextColor(
                BG
        );

        back.setTextSize(
                16
        );

        back.setAllCaps(
                false
        );

        back.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        back.setBackgroundResource(
                R.drawable.neon_button
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

        setContentView(root);
    }

    /*
     * زر الرجوع في أندرويد
     */
    @Override
    public void onBackPressed() {

        if (currentData != null) {

            showHome();

            return;
        }

        confirmExit();
    }

    /*
     * تسجيل ذكر مكتمل في الحصيلة اليومية
     */
    public static void recordDhikr(
            Context context
    ) {

        SharedPreferences p =
                context.getSharedPreferences(
                        "settings",
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
     * تنسيق تقدم التكرار
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
     * إنشاء TextView موحد
     */
    private TextView label(
            String text,
            int color,
            float size,
            boolean bold
    ) {

        TextView t =
                new TextView(this);

        t.setText(text);

        t.setTextColor(color);

        t.setTextSize(size);

        t.setGravity(
                Gravity.CENTER_VERTICAL
        );

        t.setTypeface(
                Typeface.DEFAULT,
                bold
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );

        t.setIncludeFontPadding(true);

        return t;
    }

    /*
     * تحويل dp إلى px
     */
    private int dp(int value) {

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