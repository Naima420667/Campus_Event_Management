package com.example.campuseventmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EventListActivity extends AppCompatActivity {

    private Button techFestButton, programmingButton, culturalButton;
    private Button debateButton, roboticsButton, careerButton;
    private Button sportsButton, artButton, scienceButton, musicButton;
    private Button businessButton, bloodDonationButton, addNewEventButton, manageEventsButton;

    private TextView backToHomeButton;
    private EditText eventSearchInput;
    private Spinner categorySpinner;
    private Button clearSearchButton;
    private LinearLayout pageContainer;

    private static final String[] API_URLS = {
            "http://10.0.2.2/eventnest_api/api.php",
            "http://192.168.0.104/eventnest_api/api.php"
    };

    private final String[] categories = {
            "All Categories",
            "TECHNOLOGY",
            "CODING",
            "CULTURE",
            "DEBATE",
            "ROBOTICS",
            "CAREER",
            "SPORTS",
            "ART & MEDIA",
            "SCIENCE",
            "MUSIC",
            "BUSINESS",
            "HEALTH & SOCIAL",
            "DATABASE EVENT"
    };

    private final Set<String> cardCategories = new HashSet<>(Arrays.asList(
            "TECHNOLOGY",
            "CODING",
            "CULTURE",
            "DEBATE",
            "ROBOTICS",
            "CAREER",
            "SPORTS",
            "ART & MEDIA",
            "SCIENCE",
            "MUSIC",
            "BUSINESS",
            "HEALTH & SOCIAL",
            "DATABASE EVENT",
            "CUSTOM"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        backToHomeButton = findViewById(R.id.backToHomeButton);

        techFestButton = findViewById(R.id.techFestButton);
        programmingButton = findViewById(R.id.programmingButton);
        culturalButton = findViewById(R.id.culturalButton);
        debateButton = findViewById(R.id.debateButton);
        roboticsButton = findViewById(R.id.roboticsButton);
        careerButton = findViewById(R.id.careerButton);
        sportsButton = findViewById(R.id.sportsButton);
        artButton = findViewById(R.id.artButton);
        scienceButton = findViewById(R.id.scienceButton);
        musicButton = findViewById(R.id.musicButton);
        businessButton = findViewById(R.id.businessButton);
        bloodDonationButton = findViewById(R.id.bloodDonationButton);
        addNewEventButton = findViewById(R.id.addNewEventButton);

        eventSearchInput = findViewById(R.id.eventSearchInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        clearSearchButton = findViewById(R.id.clearSearchButton);

        ScrollView scrollView = findViewById(R.id.eventListScrollView);

        if (scrollView == null) {
            View root = ((android.view.ViewGroup)
                    findViewById(android.R.id.content)).getChildAt(0);

            if (root instanceof ScrollView) {
                scrollView = (ScrollView) root;
            }
        }

        if (scrollView != null
                && scrollView.getChildAt(0) instanceof LinearLayout) {
            pageContainer = (LinearLayout) scrollView.getChildAt(0);
        }

        if (backToHomeButton != null) {
            backToHomeButton.setOnClickListener(v -> finish());
        }

        if (techFestButton != null)
            techFestButton.setOnClickListener(v -> openEventDetails("tech"));

        if (programmingButton != null)
            programmingButton.setOnClickListener(v -> openEventDetails("programming"));

        if (culturalButton != null)
            culturalButton.setOnClickListener(v -> openEventDetails("cultural"));

        if (debateButton != null)
            debateButton.setOnClickListener(v -> openEventDetails("debate"));

        if (roboticsButton != null)
            roboticsButton.setOnClickListener(v -> openEventDetails("robotics"));

        if (careerButton != null)
            careerButton.setOnClickListener(v -> openEventDetails("career"));

        if (sportsButton != null)
            sportsButton.setOnClickListener(v -> openEventDetails("sports"));

        if (artButton != null)
            artButton.setOnClickListener(v -> openEventDetails("art"));

        if (scienceButton != null)
            scienceButton.setOnClickListener(v -> openEventDetails("science"));

        if (musicButton != null)
            musicButton.setOnClickListener(v -> openEventDetails("music"));

        if (businessButton != null)
            businessButton.setOnClickListener(v -> openEventDetails("business"));

        if (bloodDonationButton != null) {
            bloodDonationButton.setOnClickListener(
                    v -> openEventDetails("blood_donation"));
        }

        if (addNewEventButton != null) {
            addNewEventButton.setOnClickListener(v -> {
                Intent intent = new Intent(
                        EventListActivity.this,
                        AddEventActivity.class
                );
                startActivity(intent);
            });
        }

        manageEventsButton = findViewById(R.id.manageEventsButton);

        if (manageEventsButton != null) {
            manageEventsButton.setOnClickListener(v -> {
                Intent intent = new Intent(
                        EventListActivity.this,
                        ManageEventsActivity.class
                );
                startActivity(intent);
            });
        }

        setupSearchAndFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDatabaseEvents();
        loadCustomEvents();
        setupDeleteOptionForCards();
    }

    private void loadDatabaseEvents() {
        if (pageContainer == null) return;

        new Thread(() -> {
            JSONArray events = null;

            for (String apiUrl : API_URLS) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(apiUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(4000);
                    connection.setReadTimeout(4000);

                    InputStream input = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    input.close();

                    String respStr = response.toString().trim();
                    if (respStr.startsWith("[")) {
                        events = new JSONArray(respStr);
                    } else if (respStr.startsWith("{")) {
                        JSONObject json = new JSONObject(respStr);
                        if (json.has("events")) {
                            events = json.optJSONArray("events");
                        } else if (json.has("data")) {
                            events = json.optJSONArray("data");
                        } else if (json.has("result")) {
                            events = json.optJSONArray("result");
                        }
                    }

                    if (events != null && events.length() > 0) {
                        break;
                    }
                } catch (Exception e) {
                    // Try next URL
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            final JSONArray finalEvents = events;

            runOnUiThread(() -> {
                if (finalEvents != null) {
                    removeOldDatabaseCards();

                    for (int i = 0; i < finalEvents.length(); i++) {
                        JSONObject event = finalEvents.optJSONObject(i);
                        if (event != null) {
                            addDatabaseEventCard(event);
                        }
                    }

                    filterEvents();
                }
            });
        }).start();
    }

    private void removeOldDatabaseCards() {
        if (pageContainer == null) return;

        for (int i = pageContainer.getChildCount() - 1; i >= 0; i--) {
            View child = pageContainer.getChildAt(i);
            Object tag = child.getTag();

            if (tag instanceof String && ((String) tag).startsWith("db_event_")) {
                pageContainer.removeViewAt(i);
            }
        }
    }

    private void addDatabaseEventCard(JSONObject event) {
        int eventId = event.optInt("id", event.optInt("event_id", -1));
        String name = event.optString("event_name", event.optString("name", "Untitled Event"));
        String date = event.optString("event_date", event.optString("date", "Date unavailable"));
        String venue = event.optString("venue", "Venue unavailable");
        String organizer = event.optString("organizer", "Not specified");

        LinearLayout card = new LinearLayout(this);
        card.setTag("db_event_" + eventId);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dp(16));
        cardBg.setStroke(2, Color.parseColor("#E2E8F0"));
        card.setBackground(cardBg);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cardParams);

        ImageView bannerImg = new ImageView(this);
        bannerImg.setImageResource(R.drawable.event_banner_blue);
        bannerImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(90)
        );
        imgParams.bottomMargin = dp(6);
        bannerImg.setLayoutParams(imgParams);
        card.addView(bannerImg);

        TextView category = new TextView(this);
        category.setText("✦ DATABASE EVENT");
        category.setTextColor(Color.parseColor("#3155D4"));
        category.setTextSize(10);
        category.setTypeface(null, Typeface.BOLD);
        card.addView(category);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(Color.parseColor("#172554"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, dp(4), 0, dp(4));
        card.addView(title);

        TextView details = new TextView(this);
        details.setText("📅  " + date + "  •  📍  " + venue + " (" + organizer + ")");
        details.setTextColor(Color.parseColor("#64748B"));
        details.setTextSize(12);
        card.addView(details);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = dp(10);
        buttonRow.setLayoutParams(rowParams);

        Button viewDetailsButton = new Button(this);
        viewDetailsButton.setText("View Details  →");
        viewDetailsButton.setAllCaps(false);
        viewDetailsButton.setTextColor(Color.WHITE);
        viewDetailsButton.setTextSize(13);
        viewDetailsButton.setTypeface(null, Typeface.BOLD);
        viewDetailsButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#3155D4")));
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                0, dp(42), 1f
        );
        viewParams.rightMargin = dp(6);
        viewDetailsButton.setLayoutParams(viewParams);
        viewDetailsButton.setOnClickListener(v -> openCustomEventDetails(name, date, venue, organizer));
        buttonRow.addView(viewDetailsButton);

        Button deleteBtn = new Button(this);
        deleteBtn.setText("🗑️");
        deleteBtn.setAllCaps(false);
        deleteBtn.setTextColor(Color.WHITE);
        deleteBtn.setTextSize(13);
        deleteBtn.setTypeface(null, Typeface.BOLD);
        deleteBtn.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#374151"))
        );
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                dp(48), dp(42)
        );
        deleteBtn.setLayoutParams(deleteParams);
        final int finalEventId = eventId;
        deleteBtn.setOnClickListener(v -> confirmDeleteDatabaseEvent(finalEventId, name, card));
        buttonRow.addView(deleteBtn);

        card.addView(buttonRow);
        pageContainer.addView(card, 0); // Insert at top of event list
    }

    private void confirmDeleteDatabaseEvent(int eventId, String eventTitle, View cardView) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Database Event")
                .setMessage("Are you sure you want to delete \"" + eventTitle + "\" from MySQL database?")
                .setPositiveButton("Delete", (dialog, which) -> deleteDatabaseEvent(eventId, eventTitle, cardView))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDatabaseEvent(int eventId, String eventTitle, View cardView) {
        cardView.setVisibility(View.GONE);

        new Thread(() -> {
            for (String apiUrl : API_URLS) {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(4000);
                    connection.setReadTimeout(4000);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                    String postData = "action=delete&id=" + eventId + "&event_id=" + eventId;
                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(postData.getBytes(StandardCharsets.UTF_8));
                        output.flush();
                    }

                    int responseCode = connection.getResponseCode();
                    connection.disconnect();
                    if (responseCode >= 200 && responseCode < 400) {
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(EventListActivity.this, "Deleted \"" + eventTitle + "\"", Toast.LENGTH_SHORT).show();
                loadDatabaseEvents();
            });
        }).start();
    }

    private void setupSearchAndFilter() {
        if (categorySpinner == null
                || eventSearchInput == null
                || clearSearchButton == null) {
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        categorySpinner.setAdapter(adapter);

        eventSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                filterEvents();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        categorySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        filterEvents();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        clearSearchButton.setOnClickListener(v -> {
            eventSearchInput.setText("");
            categorySpinner.setSelection(0);
            filterEvents();
        });
    }

    private void filterEvents() {
        if (pageContainer == null
                || eventSearchInput == null
                || categorySpinner == null) {
            return;
        }

        String query = eventSearchInput.getText()
                .toString().trim().toLowerCase();

        String selectedCategory =
                categorySpinner.getSelectedItem() == null
                        ? "All Categories"
                        : categorySpinner.getSelectedItem().toString();

        int visibleCount = 0;

        for (int i = 0; i < pageContainer.getChildCount(); i++) {
            View child = pageContainer.getChildAt(i);

            if (!(child instanceof LinearLayout)) continue;

            LinearLayout container = (LinearLayout) child;

            // If it's a horizontal row, filter its children
            if (container.getOrientation() == LinearLayout.HORIZONTAL) {
                for (int j = 0; j < container.getChildCount(); j++) {
                    View col = container.getChildAt(j);
                    if (col instanceof LinearLayout) {
                        boolean match = filterSingleCard((LinearLayout) col, query, selectedCategory);
                        if (match) visibleCount++;
                    }
                }
            } else {
                boolean match = filterSingleCard(container, query, selectedCategory);
                if (match) visibleCount++;
            }
        }

        TextView noResults = findViewById(R.id.noEventsFoundText);
        if (noResults != null) {
            noResults.setVisibility(
                    visibleCount == 0 ? View.VISIBLE : View.GONE
            );
        }
    }

    private boolean filterSingleCard(LinearLayout card, String query, String selectedCategory) {
        StringBuilder cardText = new StringBuilder();

        for (int j = 0; j < card.getChildCount(); j++) {
            View item = card.getChildAt(j);
            if (item instanceof TextView) {
                cardText.append(((TextView) item).getText().toString()).append(" ");
            } else if (item instanceof Button) {
                cardText.append(((Button) item).getText().toString()).append(" ");
            } else if (item instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) item;
                for (int k = 0; k < row.getChildCount(); k++) {
                    View sub = row.getChildAt(k);
                    if (sub instanceof TextView) cardText.append(((TextView) sub).getText().toString()).append(" ");
                    else if (sub instanceof Button) cardText.append(((Button) sub).getText().toString()).append(" ");
                }
            }
        }

        String cardStr = cardText.toString();
        boolean matchesSearch = query.isEmpty() || cardStr.toLowerCase().contains(query);
        boolean matchesCategory = selectedCategory.equals("All Categories")
                || cardStr.toUpperCase().contains(selectedCategory.toUpperCase());

        boolean show = matchesSearch && matchesCategory;
        card.setVisibility(show ? View.VISIBLE : View.GONE);
        return show;
    }

    private void loadCustomEvents() {
        if (pageContainer == null) return;

        SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
        String customJson = prefs.getString("custom_events", "");
        if (customJson.isEmpty()) return;

        try {
            JSONArray array = new JSONArray(customJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("name", "Custom Event");
                String date = obj.optString("date", "Upcoming");
                String venue = obj.optString("venue", "Campus");

                String tag = "custom_event_" + name;
                if (pageContainer.findViewWithTag(tag) != null) continue;

                LinearLayout card = new LinearLayout(this);
                card.setTag(tag);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(14), dp(14), dp(14), dp(14));

                GradientDrawable cardBg = new GradientDrawable();
                cardBg.setColor(Color.WHITE);
                cardBg.setCornerRadius(dp(16));
                cardBg.setStroke(2, Color.parseColor("#E2E8F0"));
                card.setBackground(cardBg);
                card.setElevation(dp(3));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, dp(16));
                card.setLayoutParams(params);

                ImageView bannerImg = new ImageView(this);
                bannerImg.setImageResource(R.drawable.event_banner_blue);
                bannerImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(90)
                );
                imgParams.bottomMargin = dp(6);
                bannerImg.setLayoutParams(imgParams);
                card.addView(bannerImg);

                TextView categoryText = new TextView(this);
                categoryText.setText("✦ CUSTOM");
                categoryText.setTextColor(Color.parseColor("#3155D4"));
                categoryText.setTextSize(10);
                categoryText.setTypeface(null, Typeface.BOLD);
                card.addView(categoryText);

                TextView titleText = new TextView(this);
                titleText.setText(name);
                titleText.setTextColor(Color.parseColor("#172554"));
                titleText.setTextSize(18);
                titleText.setTypeface(null, Typeface.BOLD);
                card.addView(titleText);

                TextView infoText = new TextView(this);
                infoText.setText("📅  " + date + "  •  📍  " + venue);
                infoText.setTextColor(Color.parseColor("#64748B"));
                infoText.setTextSize(12);
                card.addView(infoText);

                LinearLayout buttonRow = new LinearLayout(this);
                buttonRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.topMargin = dp(10);
                buttonRow.setLayoutParams(rowParams);

                Button btn = new Button(this);
                btn.setText("View Details  →");
                btn.setAllCaps(false);
                btn.setTextColor(Color.WHITE);
                btn.setTextSize(13);
                btn.setTypeface(null, Typeface.BOLD);
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3155D4")));
                LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                        0, dp(42), 1f
                );
                viewParams.rightMargin = dp(6);
                btn.setLayoutParams(viewParams);
                String organizer = obj.optString("organizer", "Campus Committee");
                btn.setOnClickListener(v -> openCustomEventDetails(name, date, venue, organizer));
                buttonRow.addView(btn);

                Button deleteBtn = new Button(this);
                deleteBtn.setText("🗑️");
                deleteBtn.setAllCaps(false);
                deleteBtn.setTextColor(Color.WHITE);
                deleteBtn.setTextSize(13);
                deleteBtn.setTypeface(null, Typeface.BOLD);
                deleteBtn.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#374151"))
                );
                LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                        dp(48), dp(42)
                );
                deleteBtn.setLayoutParams(deleteParams);
                deleteBtn.setOnClickListener(v -> confirmDeleteEvent(name, card));
                buttonRow.addView(deleteBtn);

                card.addView(buttonRow);

                int insertIndex = Math.min(6, pageContainer.getChildCount());
                pageContainer.addView(card, insertIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDeleteOptionForCards() {
        if (pageContainer == null) return;

        SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
        Set<String> deletedEvents = prefs.getStringSet("deleted_events_set", new HashSet<>());

        processContainerCards(pageContainer, deletedEvents);
    }

    private void processContainerCards(LinearLayout container, Set<String> deletedEvents) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (!(child instanceof LinearLayout)) continue;

            LinearLayout layout = (LinearLayout) child;

            // If layout is a horizontal row, process its child cards
            if (layout.getOrientation() == LinearLayout.HORIZONTAL && layout.getChildCount() >= 1) {
                processContainerCards(layout, deletedEvents);
                continue;
            }

            // It's an event card
            if (layout.getChildCount() < 2) continue;

            // Find event title view inside card
            TextView titleView = null;
            for (int k = 0; k < layout.getChildCount(); k++) {
                View sub = layout.getChildAt(k);
                if (sub instanceof TextView) {
                    String txt = ((TextView) sub).getText().toString().trim();
                    if (!txt.isEmpty() && !txt.startsWith("✦") && !cardCategories.contains(txt)) {
                        titleView = (TextView) sub;
                        break;
                    }
                }
            }

            if (titleView == null) continue;

            String title = titleView.getText().toString().trim();

            if (deletedEvents.contains(title)) {
                layout.setVisibility(View.GONE);
                continue;
            }

            // Apply modern card background & elevation
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(Color.WHITE);
            cardBg.setCornerRadius(dp(16));
            cardBg.setStroke(2, Color.parseColor("#E2E8F0"));
            layout.setBackground(cardBg);
            layout.setElevation(dp(3));

            // Convert stacked buttons into a single horizontal row if needed
            View lastChild = layout.getChildAt(layout.getChildCount() - 1);
            if (lastChild instanceof Button && !(lastChild.getParent() instanceof LinearLayout && ((LinearLayout) lastChild.getParent()).getOrientation() == LinearLayout.HORIZONTAL)) {
                Button detailsBtn = (Button) lastChild;
                layout.removeView(detailsBtn);

                LinearLayout buttonRow = new LinearLayout(this);
                buttonRow.setTag("button_row_" + title);
                buttonRow.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.topMargin = dp(10);
                buttonRow.setLayoutParams(rowParams);

                LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                        0, dp(42), 1f
                );
                viewParams.rightMargin = dp(6);
                detailsBtn.setLayoutParams(viewParams);
                buttonRow.addView(detailsBtn);

                Button deleteBtn = new Button(this);
                deleteBtn.setText("🗑️");
                deleteBtn.setAllCaps(false);
                deleteBtn.setTextSize(13);
                deleteBtn.setTypeface(null, Typeface.BOLD);
                deleteBtn.setTextColor(Color.WHITE);
                deleteBtn.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#374151"))
                );

                LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                        dp(48), dp(42)
                );
                deleteBtn.setLayoutParams(deleteParams);
                deleteBtn.setOnClickListener(v -> confirmDeleteEvent(title, layout));
                buttonRow.addView(deleteBtn);

                layout.addView(buttonRow);
            }
        }
    }

    private void confirmDeleteEvent(String eventTitle, View cardView) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + eventTitle + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(eventTitle, cardView))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(String eventTitle, View cardView) {
        cardView.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("CampusEvents", MODE_PRIVATE);
        Set<String> deletedEvents = new HashSet<>(prefs.getStringSet("deleted_events_set", new HashSet<>()));
        deletedEvents.add(eventTitle);

        String customJson = prefs.getString("custom_events", "");
        if (!customJson.isEmpty()) {
            try {
                JSONArray array = new JSONArray(customJson);
                JSONArray updatedArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (!eventTitle.equalsIgnoreCase(obj.optString("name", ""))) {
                        updatedArray.put(obj);
                    }
                }
                prefs.edit().putString("custom_events", updatedArray.toString()).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        prefs.edit().putStringSet("deleted_events_set", deletedEvents).apply();
        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
    }

    private void openEventDetails(String eventCode) {
        Intent intent = new Intent(
                EventListActivity.this,
                EventDetailsActivity.class
        );

        intent.putExtra("event", eventCode);
        startActivity(intent);
    }

    private void openCustomEventDetails(String title, String date, String venue, String organizer) {
        Intent intent = new Intent(
                EventListActivity.this,
                EventDetailsActivity.class
        );

        intent.putExtra("event_name", title);
        intent.putExtra("custom_name", title);
        intent.putExtra("event_date", date);
        intent.putExtra("custom_date", date);
        intent.putExtra("venue", venue);
        intent.putExtra("custom_venue", venue);
        intent.putExtra("organizer", organizer);
        intent.putExtra("custom_organizer", organizer);
        startActivity(intent);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
