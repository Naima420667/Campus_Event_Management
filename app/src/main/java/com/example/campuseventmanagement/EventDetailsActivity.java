package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {

    private LinearLayout eventContainer;

    private final int navy = Color.parseColor("#172554");
    private final int blue = Color.parseColor("#3155D4");
    private final int gray = Color.parseColor("#64748B");
    private final int green = Color.parseColor("#16A34A");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventContainer = findViewById(R.id.eventContainer);

        Intent intent = getIntent();
        processAndDisplayEvent(intent);
    }

    private void processAndDisplayEvent(Intent intent) {
        if (intent == null) {
            Toast.makeText(this, "No event details available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Extract raw string parameters from all possible intent extra keys
        String rawTitle = getFirstNonEmptyExtra(intent, "event_name", "custom_name", "title", "name", "event");
        String rawDate = getFirstNonEmptyExtra(intent, "event_date", "custom_date", "date");
        String rawVenue = getFirstNonEmptyExtra(intent, "venue", "custom_venue", "location");
        String rawOrganizer = getFirstNonEmptyExtra(intent, "organizer", "custom_organizer");
        String rawDescription = getFirstNonEmptyExtra(intent, "description", "custom_description");
        String rawIcon = getFirstNonEmptyExtra(intent, "icon", "custom_icon");
        String rawStatus = getFirstNonEmptyExtra(intent, "status");

        int eventId = intent.getIntExtra("event_id", intent.getIntExtra("id", 1));

        // Preset defaults structure
        String title = "Campus Event";
        String date = "Upcoming";
        String venue = "University Campus";
        String organizer = "EventNest Committee";
        String description = "";
        String icon = "⚡";
        String status = "Upcoming";

        // Attempt preset resolution based on title or event code key
        String searchKey = (rawTitle != null) ? rawTitle : "";
        EventPreset preset = resolvePreset(searchKey);

        if (preset != null) {
            title = preset.title;
            date = preset.date;
            venue = preset.venue;
            organizer = preset.organizer;
            description = preset.description;
            icon = preset.icon;
            if (eventId <= 1) {
                eventId = preset.id;
            }
        } else if (rawTitle != null && !rawTitle.trim().isEmpty() && !rawTitle.equalsIgnoreCase("custom")) {
            title = rawTitle.trim();
        }

        // Apply dynamic overrides if passed explicitly
        if (rawTitle != null && !rawTitle.trim().isEmpty() && !rawTitle.equalsIgnoreCase("custom") && !rawTitle.equalsIgnoreCase("event")) {
            title = rawTitle.trim();
        }

        if (rawDate != null && !rawDate.trim().isEmpty()) {
            date = rawDate.trim();
        }

        if (rawVenue != null && !rawVenue.trim().isEmpty()) {
            venue = rawVenue.trim();
        }

        if (rawOrganizer != null && !rawOrganizer.trim().isEmpty()) {
            organizer = rawOrganizer.trim();
        }

        if (rawDescription != null && !rawDescription.trim().isEmpty()) {
            description = rawDescription.trim();
        } else if (description.isEmpty()) {
            description = "Official " + title + " event taking place on campus. "
                    + "Venue: " + venue + ". Organized by " + organizer + ". "
                    + "Join us to connect, discover, and participate with fellow university students!";
        }

        if (rawIcon != null && !rawIcon.trim().isEmpty()) {
            icon = rawIcon.trim();
        }

        if (rawStatus != null && !rawStatus.trim().isEmpty()) {
            status = rawStatus.trim();
        }

        displayEventDetailsUI(eventId, title, date, venue, organizer, description, icon, status);
    }

    private String getFirstNonEmptyExtra(Intent intent, String... keys) {
        for (String key : keys) {
            if (intent.hasExtra(key)) {
                String val = intent.getStringExtra(key);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        return null;
    }

    private void displayEventDetailsUI(
            int eventId,
            String title,
            String date,
            String venue,
            String organizer,
            String description,
            String icon,
            String status) {

        if (eventContainer == null) return;
        eventContainer.removeAllViews();

        // 1. Navigation Header Bar
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(0, 0, 0, dpToPx(14));

        TextView backButton = new TextView(this);
        backButton.setText("← Back to Events");
        backButton.setTextColor(blue);
        backButton.setTextSize(15);
        backButton.setTypeface(null, Typeface.BOLD);
        backButton.setPadding(dpToPx(4), dpToPx(4), dpToPx(12), dpToPx(4));
        backButton.setOnClickListener(v -> finish());
        topBar.addView(backButton);

        TextView brandTag = new TextView(this);
        brandTag.setText("✦ EVENTNEST");
        brandTag.setTextColor(navy);
        brandTag.setTextSize(13);
        brandTag.setTypeface(null, Typeface.BOLD);
        brandTag.setGravity(Gravity.END);
        LinearLayout.LayoutParams brandParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        brandTag.setLayoutParams(brandParams);
        topBar.addView(brandTag);

        eventContainer.addView(topBar);

        // 2. Banner Image
        ImageView bannerImg = new ImageView(this);
        bannerImg.setImageResource(R.drawable.event_banner_blue);
        bannerImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable bannerBg = new GradientDrawable();
        bannerBg.setCornerRadius(dpToPx(16));
        bannerImg.setClipToOutline(true);
        bannerImg.setBackground(bannerBg);

        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(140)
        );
        imgParams.setMargins(0, 0, 0, dpToPx(16));
        bannerImg.setLayoutParams(imgParams);
        eventContainer.addView(bannerImg);

        // 3. Hero Card Title Section
        LinearLayout heroCard = new LinearLayout(this);
        heroCard.setOrientation(LinearLayout.VERTICAL);
        heroCard.setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(16));

        GradientDrawable heroBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#EEF2FF"), Color.WHITE}
        );
        heroBg.setCornerRadius(dpToPx(18));
        heroBg.setStroke(2, Color.parseColor("#C7D2FE"));
        heroCard.setBackground(heroBg);

        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        heroParams.setMargins(0, 0, 0, dpToPx(16));
        heroCard.setLayoutParams(heroParams);

        TextView titleView = new TextView(this);
        titleView.setText(icon + "  " + title);
        titleView.setTextColor(navy);
        titleView.setTextSize(22);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        heroCard.addView(titleView);

        eventContainer.addView(heroCard);

        // 4. 2x2 Information Cards Grid
        LinearLayout gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);

        // Row 1: Date & Venue
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row1Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        row1Params.bottomMargin = dpToPx(10);
        row1.setLayoutParams(row1Params);

        addGridCard(row1, "📅 EVENT DATE", date, blue, "#F0F5FF");
        addGridCard(row1, "📍 LOCATION", venue, blue, "#F0F5FF");
        gridContainer.addView(row1);

        // Row 2: Organizer & Status
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row2Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        row2Params.bottomMargin = dpToPx(16);
        row2.setLayoutParams(row2Params);

        addGridCard(row2, "👤 ORGANIZER", organizer, navy, "#F8FAFC");
        addGridCard(row2, "🏷️ STATUS", status.toUpperCase(), green, "#F0FDF4");
        gridContainer.addView(row2);

        eventContainer.addView(gridContainer);

        // 5. About Event Section
        TextView aboutTitle = new TextView(this);
        aboutTitle.setText("ABOUT THIS EVENT");
        aboutTitle.setTextColor(blue);
        aboutTitle.setTextSize(12);
        aboutTitle.setTypeface(null, Typeface.BOLD);
        aboutTitle.setPadding(0, 0, 0, dpToPx(6));
        eventContainer.addView(aboutTitle);

        TextView aboutText = new TextView(this);
        aboutText.setText(description);
        aboutText.setTextColor(gray);
        aboutText.setTextSize(14);
        aboutText.setLineSpacing(3, 1.15f);
        aboutText.setPadding(0, 0, 0, dpToPx(20));
        eventContainer.addView(aboutText);

        // 6. Action Buttons Section
        final int finalEventId = (eventId <= 0) ? 1 : eventId;
        final String finalTitle = title;
        final String finalDate = date;
        final String finalVenue = venue;
        final String finalOrganizer = organizer;
        final String finalDescription = description;

        // Button 1: Register for Event
        Button registerButton = createActionButton("⚡  Register for Event  →", blue);
        registerButton.setOnClickListener(v -> {
            Intent regIntent = new Intent(EventDetailsActivity.this, RegistrationActivity.class);
            regIntent.putExtra("event_id", finalEventId);
            regIntent.putExtra("event_name", finalTitle);
            regIntent.putExtra("event_date", finalDate);
            regIntent.putExtra("venue", finalVenue);
            regIntent.putExtra("organizer", finalOrganizer);
            startActivity(regIntent);
        });
        addActionButtonToContainer(registerButton);

        // Button 2: View Event Pass
        Button passButton = createActionButton("🎟️  View Event Pass", navy);
        passButton.setOnClickListener(v -> {
            Intent passIntent = new Intent(EventDetailsActivity.this, EventPassActivity.class);
            passIntent.putExtra("eventName", finalTitle);
            passIntent.putExtra("eventDate", finalDate);
            passIntent.putExtra("eventVenue", finalVenue);
            passIntent.putExtra("passId", "PASS-" + finalEventId + "-2026");
            passIntent.putExtra("event_id", String.valueOf(finalEventId));
            passIntent.putExtra("organizer", finalOrganizer);
            startActivity(passIntent);
        });
        addActionButtonToContainer(passButton);

        // Button 3: Set Event Reminder
        Button reminderButton = createActionButton("⏰  Set Event Reminder", Color.parseColor("#7C3AED"));
        reminderButton.setOnClickListener(v -> {
            Intent reminderIntent = new Intent(EventDetailsActivity.this, EventReminderActivity.class);
            reminderIntent.putExtra("event_name", finalTitle);
            reminderIntent.putExtra("event_date", finalDate);
            startActivity(reminderIntent);
        });
        addActionButtonToContainer(reminderButton);

        // Button 4: Add to Device Calendar
        Button calendarButton = createActionButton("📅  Add to Device Calendar", Color.parseColor("#0F766E"));
        calendarButton.setOnClickListener(v -> {
            try {
                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                calIntent.setData(CalendarContract.Events.CONTENT_URI);
                calIntent.putExtra(CalendarContract.Events.TITLE, finalTitle);
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, finalVenue);
                calIntent.putExtra(CalendarContract.Events.DESCRIPTION, finalDescription);
                startActivity(calIntent);
            } catch (Exception e) {
                Toast.makeText(EventDetailsActivity.this, "Calendar app not found.", Toast.LENGTH_SHORT).show();
            }
        });
        addActionButtonToContainer(calendarButton);

        // Button 5: Share Event
        Button shareButton = createActionButton("↗  Share Event with Friends", Color.parseColor("#2563EB"));
        shareButton.setOnClickListener(v -> {
            String shareText = "EventNest - " + finalTitle
                    + "\nDate: " + finalDate
                    + "\nVenue: " + finalVenue
                    + "\nOrganizer: " + finalOrganizer;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            try {
                startActivity(Intent.createChooser(shareIntent, "Share Event"));
            } catch (Exception e) {
                Toast.makeText(EventDetailsActivity.this, "Sharing unavailable.", Toast.LENGTH_SHORT).show();
            }
        });
        addActionButtonToContainer(shareButton);
    }

    private void addActionButtonToContainer(Button button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(50)
        );
        params.setMargins(0, 0, 0, dpToPx(10));
        button.setLayoutParams(params);
        eventContainer.addView(button);
    }

    private Button createActionButton(String text, int bgColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setTypeface(null, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        return button;
    }

    private void addGridCard(LinearLayout parentRow, String label, String value, int labelColor, String bgColorHex) {
        LinearLayout colCard = new LinearLayout(this);
        colCard.setOrientation(LinearLayout.VERTICAL);
        colCard.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColorHex));
        bg.setCornerRadius(dpToPx(14));
        bg.setStroke(2, Color.parseColor("#E2E8F0"));
        colCard.setBackground(bg);

        LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        colParams.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        colCard.setLayoutParams(colParams);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(labelColor);
        labelView.setTextSize(11);
        labelView.setTypeface(null, Typeface.BOLD);
        colCard.addView(labelView);

        TextView valView = new TextView(this);
        valView.setText(value);
        valView.setTextColor(navy);
        valView.setTextSize(13);
        valView.setTypeface(null, Typeface.BOLD);
        valView.setSingleLine(false);
        valView.setMaxLines(3);
        valView.setPadding(0, dpToPx(4), 0, 0);
        colCard.addView(valView);

        parentRow.addView(colCard);
    }

    private EventPreset resolvePreset(String key) {
        if (key == null || key.trim().isEmpty()) return null;
        String lower = key.trim().toLowerCase();

        if (lower.contains("tech") || lower.contains("technology")) {
            return new EventPreset(
                    5,
                    "Tech Fest 2026",
                    "15 October 2026",
                    "University Auditorium",
                    "CSE Department",
                    "A technology festival featuring innovation, tech project exhibitions, hackathons and guest lectures.",
                    "⚡"
            );
        } else if (lower.contains("programming") || lower.contains("coding")) {
            return new EventPreset(
                    1,
                    "Programming Contest",
                    "20 October 2026",
                    "CSE Lab",
                    "CSE Department",
                    "A competitive programming contest designed to challenge students' problem-solving skills and algorithmic thinking.",
                    "💻"
            );
        } else if (lower.contains("cultural") || lower.contains("culture")) {
            return new EventPreset(
                    2,
                    "Cultural Night",
                    "25 October 2026",
                    "University Auditorium",
                    "Cultural Club",
                    "An exciting evening filled with music, dance, drama and vibrant cultural performances.",
                    "🎭"
            );
        } else if (lower.contains("debate")) {
            return new EventPreset(
                    6,
                    "Inter-University Debate",
                    "30 October 2026",
                    "Seminar Hall",
                    "Debate Club",
                    "A competitive debate event bringing students together to share arguments and critical thinking skills.",
                    "🎤"
            );
        } else if (lower.contains("robotics")) {
            return new EventPreset(
                    7,
                    "Robotics Workshop",
                    "05 November 2026",
                    "Robotics Lab",
                    "Robotics Club",
                    "A hands-on robotics workshop where students can build hardware projects and explore practical automation concepts.",
                    "🤖"
            );
        } else if (lower.contains("career")) {
            return new EventPreset(
                    3,
                    "Career Seminar",
                    "10 November 2026",
                    "Conference Hall",
                    "Career Club",
                    "A career-focused seminar covering resume building, interview preparation, professional development and networking.",
                    "🚀"
            );
        } else if (lower.contains("sports")) {
            return new EventPreset(
                    4,
                    "Sports Carnival",
                    "15 November 2026",
                    "University Playground",
                    "Sports Club",
                    "A fun-filled annual sports carnival featuring football, cricket, athletics and outdoor competitions.",
                    "🏆"
            );
        } else if (lower.contains("art") || lower.contains("media")) {
            return new EventPreset(
                    8,
                    "Art & Photography Exhibition",
                    "20 November 2026",
                    "Art Gallery",
                    "Art Club",
                    "An exhibition showcasing creative artwork, digital design and photography created by talented students.",
                    "🎨"
            );
        } else if (lower.contains("science")) {
            return new EventPreset(
                    9,
                    "Science & Innovation Fair",
                    "25 November 2026",
                    "Science Complex",
                    "Science Club",
                    "An exhibition showcasing innovative scientific research, prototype experiments and student inventions.",
                    "🧪"
            );
        } else if (lower.contains("music")) {
            return new EventPreset(
                    10,
                    "Campus Music Fest",
                    "01 December 2026",
                    "Open Air Stage",
                    "Music Club",
                    "A lively musical concert highlighting student bands, solo vocalists, acoustic sets and live music.",
                    "🎵"
            );
        } else if (lower.contains("business") || lower.contains("pitch")) {
            return new EventPreset(
                    11,
                    "Business Pitch Competition",
                    "05 December 2026",
                    "Business Auditorium",
                    "Entrepreneurship Club",
                    "A startup pitch competition where students present innovative business ideas and business models to industry judges.",
                    "📈"
            );
        } else if (lower.contains("blood") || lower.contains("health") || lower.contains("social")) {
            return new EventPreset(
                    12,
                    "Blood Donation & Health Camp",
                    "10 December 2026",
                    "Student Centre",
                    "Red Crescent Youth",
                    "A community-focused blood donation drive accompanied by free health checkups and awareness sessions.",
                    "🩸"
            );
        }

        return null;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class EventPreset {
        int id;
        String title;
        String date;
        String venue;
        String organizer;
        String description;
        String icon;

        EventPreset(int id, String title, String date, String venue, String organizer, String description, String icon) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.venue = venue;
            this.organizer = organizer;
            this.description = description;
            this.icon = icon;
        }
    }
}
