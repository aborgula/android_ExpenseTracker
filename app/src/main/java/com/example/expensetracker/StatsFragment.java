package com.example.expensetracker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatsFragment extends Fragment {

    private MaterialButton btnToday, btnYesterday, btnWeek, btnMonth, btnYear;
    private final String activeColor = "#63B1A1";
    private final String textActive = "#FFFFFF";
    private final String textInactive = "#63B1A1";
    private String currentFilterType = "today";
    private com.github.mikephil.charting.charts.LineChart lineChart;
    private TextView textTotal;

    private List<Expense> allExpenses = new ArrayList<>();
    private ValueEventListener expensesListener;
    private DatabaseReference expensesRef;

    public StatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        lineChart = view.findViewById(R.id.line_chart);
        setupLineChart();
        btnToday = view.findViewById(R.id.btn_today);
        btnYesterday = view.findViewById(R.id.btn_yesterday);
        btnWeek = view.findViewById(R.id.btn_week);
        btnMonth = view.findViewById(R.id.btn_month);
        btnYear = view.findViewById(R.id.btn_year);
        textTotal = view.findViewById(R.id.text_total);

        List<MaterialButton> buttons = new ArrayList<>();
        buttons.add(btnToday);
        buttons.add(btnYesterday);
        buttons.add(btnWeek);
        buttons.add(btnMonth);
        buttons.add(btnYear);

        // Obsługa kliknięć
        for (MaterialButton button : buttons) {
            button.setOnClickListener(v -> {
                for (MaterialButton b : buttons) {
                    b.setBackgroundColor(Color.TRANSPARENT);
                    b.setTextColor(Color.parseColor(textInactive));
                }
                button.setBackgroundColor(Color.parseColor(activeColor));
                button.setTextColor(Color.parseColor(textActive));

                if (button == btnToday) filterByType("today");
                else if (button == btnYesterday) filterByType("yesterday");
                else if (button == btnWeek) filterByType("week");
                else if (button == btnMonth) filterByType("month");
                else if (button == btnYear) filterByType("year");
            });
        }

        // Domyślnie pokazujemy "Today"
        btnToday.setBackgroundColor(Color.parseColor(activeColor));
        btnToday.setTextColor(Color.parseColor(textActive));

        loadUserExpenses();

        return view;
    }

    private void loadUserExpenses() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        expensesRef = FirebaseDatabase.getInstance()
                .getReference("expenses")
                .child(userId);

        expensesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allExpenses.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Expense e = dataSnapshot.getValue(Expense.class);
                    if (e != null) {
                        e.setId(dataSnapshot.getKey());
                        allExpenses.add(e);
                    }
                }
                Log.d("StatsFragment", "Loaded " + allExpenses.size() + " expenses");

                // 🔹 TYLKO odświeżaj wykres jeśli fragment jest widoczny
                if (isAdded() && getContext() != null) {
                    filterByType(currentFilterType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StatsFragment", "Failed to load: " + error.getMessage());
            }
        };

        expensesRef.addValueEventListener(expensesListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 🔹 Usuń listener gdy fragment jest niszczony
        if (expensesRef != null && expensesListener != null) {
            expensesRef.removeEventListener(expensesListener);
        }
    }

    private void filterByType(String type) {
        // 🔹 Zabezpieczenie przed wywołaniem gdy fragment nie jest aktywny
        if (!isAdded() || getContext() == null) {
            return;
        }

        List<Expense> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        currentFilterType = type;

        Calendar today = Calendar.getInstance();
        normalizeDate(today);
        Log.d("StatsFragment", "📅 Today normalized: " + sdf.format(today.getTime()));

        for (Expense e : allExpenses) {
            try {
                Date parsedDate = sdf.parse(e.getDate());
                if (parsedDate == null) continue;

                Calendar expenseCal = Calendar.getInstance();
                expenseCal.setTime(parsedDate);
                normalizeDate(expenseCal);

                long diffMillis = today.getTimeInMillis() - expenseCal.getTimeInMillis();
                long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                Log.d("StatsFragment", String.format(
                        Locale.getDefault(),
                        "Expense: %s | Date: %s | Today: %s | DiffDays: %d",
                        e.getName(),
                        sdf.format(expenseCal.getTime()),
                        sdf.format(today.getTime()),
                        diffDays
                ));

                switch (type) {
                    case "today":
                        if (diffDays == 0) filtered.add(e);
                        break;
                    case "yesterday":
                        if (diffDays == 1) filtered.add(e);
                        break;
                    case "week":
                        if (diffDays <= 7 && diffDays > 0) filtered.add(e);
                        break;
                    case "month":
                        if (diffDays <= 30 && diffDays > 0) filtered.add(e);
                        break;
                    case "year":
                        if (diffDays <= 365 && diffDays > 0) filtered.add(e);
                        break;
                }

            } catch (ParseException ex) {
                Log.e("StatsFragment", "⚠️ Date parse error for " + e.getDate() + ": " + ex.getMessage());
            }
        }

        Log.d("StatsFragment", "🔍 Filter: " + type + " → " + filtered.size() + " results");
        for (Expense e : filtered) {
            Log.d("StatsFragment", "  ✅ " + e.getName() + " | " + e.getAmount() + " | " + e.getDate());
        }

        updateLineChart(filtered);
    }

    private void normalizeDate(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getLegend().setEnabled(false);
    }

    private void updateLineChart(List<Expense> expenses) {
        // 🔹 Podwójne zabezpieczenie
        if (!isAdded() || getContext() == null) {
            Log.w("StatsFragment", "Fragment not attached, skipping chart update");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        Map<String, Float> dailySums = new TreeMap<>();

        for (Expense e : expenses) {
            try {
                Date date = sdf.parse(e.getDate());
                if (date == null) continue;
                String dayKey = sdf.format(date);

                float amount = (float) e.getAmount();
                dailySums.put(dayKey, dailySums.getOrDefault(dayKey, 0f) + amount);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        float total = 0f;
        for (float value : dailySums.values()) {
            total += value;
        }
        textTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));

        List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Float> entry : dailySums.entrySet()) {
            entries.add(new com.github.mikephil.charting.data.Entry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        com.github.mikephil.charting.data.LineDataSet dataSet = new com.github.mikephil.charting.data.LineDataSet(entries, "Daily Expenses");
        dataSet.setColor(Color.parseColor("#63B1A1"));
        dataSet.setCircleColor(Color.parseColor("#63B1A1"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(dataSet);
        lineChart.setData(lineData);

        // Formatter dla osi X
        com.github.mikephil.charting.components.XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);

        com.github.mikephil.charting.formatter.ValueFormatter xAxisFormatter = new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = Math.round(value);
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                } else {
                    return "";
                }
            }
        };

        xAxis.setValueFormatter(xAxisFormatter);

        // Tworzymy marker - teraz bezpieczne, bo sprawdziliśmy context
        ExpenseMarkerView marker = new ExpenseMarkerView(
                requireContext(),
                R.layout.marker_expense,
                xAxisFormatter
        );
        lineChart.setMarker(marker);

        lineChart.invalidate();
    }
}



/**package com.example.expensetracker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatsFragment extends Fragment {

    private MaterialButton btnToday, btnYesterday, btnWeek, btnMonth, btnYear;
    private final String activeColor = "#63B1A1"; // Miętowy kolor
    private final String textActive = "#FFFFFF";  // Biały tekst
    private final String textInactive = "#63B1A1"; // Miętowy tekst
    private String currentFilterType = "today";
    private com.github.mikephil.charting.charts.LineChart lineChart;
    private TextView textTotal;


    private List<Expense> allExpenses = new ArrayList<>();

    public StatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        lineChart = view.findViewById(R.id.line_chart);
        setupLineChart();
        btnToday = view.findViewById(R.id.btn_today);
        btnYesterday = view.findViewById(R.id.btn_yesterday);
        btnWeek = view.findViewById(R.id.btn_week);
        btnMonth = view.findViewById(R.id.btn_month);
        btnYear = view.findViewById(R.id.btn_year);
        textTotal = view.findViewById(R.id.text_total);


        List<MaterialButton> buttons = new ArrayList<>();
        buttons.add(btnToday);
        buttons.add(btnYesterday);
        buttons.add(btnWeek);
        buttons.add(btnMonth);
        buttons.add(btnYear);

        loadUserExpenses();

        // 🔹 Obsługa kliknięć
        for (MaterialButton button : buttons) {
            button.setOnClickListener(v -> {
                for (MaterialButton b : buttons) {
                    b.setBackgroundColor(Color.TRANSPARENT);
                    b.setTextColor(Color.parseColor(textInactive));
                }
                button.setBackgroundColor(Color.parseColor(activeColor));
                button.setTextColor(Color.parseColor(textActive));

                if (button == btnToday) filterByType("today");
                else if (button == btnYesterday) filterByType("yesterday");
                else if (button == btnWeek) filterByType("week");
                else if (button == btnMonth) filterByType("month");
                else if (button == btnYear) filterByType("year");
            });
        }

        // Domyślnie pokazujemy "Today"
        btnToday.setBackgroundColor(Color.parseColor(activeColor));
        btnToday.setTextColor(Color.parseColor(textActive));

        return view;
    }

    private void loadUserExpenses() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("expenses")
                .child(userId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allExpenses.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Expense e = dataSnapshot.getValue(Expense.class);
                    if (e != null) {
                        e.setId(dataSnapshot.getKey());
                        allExpenses.add(e);
                    }
                }
                Log.d("StatsFragment", "Loaded " + allExpenses.size() + " expenses");
                filterByType(currentFilterType); // zachowaj ostatni typ filtra
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StatsFragment", "Failed to load: " + error.getMessage());
            }
        });

    }



    private void filterByType(String type) {
        List<Expense> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        currentFilterType = type;

        Calendar today = Calendar.getInstance();
        normalizeDate(today);
        Log.d("StatsFragment", "📅 Today normalized: " + sdf.format(today.getTime()));

        for (Expense e : allExpenses) {
            try {
                Date parsedDate = sdf.parse(e.getDate());
                if (parsedDate == null) continue;

                Calendar expenseCal = Calendar.getInstance();
                expenseCal.setTime(parsedDate);
                normalizeDate(expenseCal);

                long diffMillis = today.getTimeInMillis() - expenseCal.getTimeInMillis();
                long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                // 🔹 LOGOWANIE szczegółowe dla debugowania
                Log.d("StatsFragment", String.format(
                        Locale.getDefault(),
                        "Expense: %s | Date: %s | Today: %s | DiffDays: %d",
                        e.getName(),
                        sdf.format(expenseCal.getTime()),
                        sdf.format(today.getTime()),
                        diffDays
                ));

                switch (type) {
                    case "today":
                        if (diffDays == 0) filtered.add(e);
                        break;
                    case "yesterday":
                        if (diffDays == 1) filtered.add(e);
                        break;
                    case "week":
                        if (diffDays <= 7 && diffDays > 0) filtered.add(e);
                        break;
                    case "month":
                        if (diffDays <= 30 && diffDays > 0) filtered.add(e);
                        break;
                    case "year":
                        if (diffDays <= 365 && diffDays > 0) filtered.add(e);
                        break;
                }

            } catch (ParseException ex) {
                Log.e("StatsFragment", "⚠️ Date parse error for " + e.getDate() + ": " + ex.getMessage());
            }
        }

        Log.d("StatsFragment", "🔍 Filter: " + type + " → " + filtered.size() + " results");
        for (Expense e : filtered) {
            Log.d("StatsFragment", "  ✅ " + e.getName() + " | " + e.getAmount() + " | " + e.getDate());
        }

        updateLineChart(filtered);
    }

    private void normalizeDate(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getLegend().setEnabled(false); // lub true, jeśli chcesz legendę
    }

   /** private void updateLineChart(List<Expense> expenses) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        Map<String, Float> dailySums = new TreeMap<>(); // TreeMap dla posortowanych dat

        for (Expense e : expenses) {
            try {
                Date date = sdf.parse(e.getDate());
                if (date == null) continue;
                String dayKey = sdf.format(date);

                float amount = (float) e.getAmount();
                dailySums.put(dayKey, dailySums.getOrDefault(dayKey, 0f) + amount);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        float total = 0f;
        for (float value : dailySums.values()) {
            total += value;
        }
        textTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));

        List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>(); // etykiety X

        int index = 0;
        for (Map.Entry<String, Float> entry : dailySums.entrySet()) {
            entries.add(new com.github.mikephil.charting.data.Entry(index, entry.getValue()));
            labels.add(entry.getKey()); // zapisujemy datę
            index++;
        }

        com.github.mikephil.charting.data.LineDataSet dataSet = new com.github.mikephil.charting.data.LineDataSet(entries, "Daily Expenses");
        dataSet.setColor(Color.parseColor("#63B1A1"));
        dataSet.setCircleColor(Color.parseColor("#63B1A1"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(dataSet);
        lineChart.setData(lineData);

        // po ustawieniu lineData
        lineChart.setData(lineData);

// Tworzymy marker
        ExpenseMarkerView marker = new ExpenseMarkerView(getContext(), R.layout.marker_expense, expenses);
        lineChart.setMarker(marker);

        lineChart.invalidate();


        // 🔹 Formatter dla osi X
        com.github.mikephil.charting.components.XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = Math.round(value);
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                } else {
                    return "";
                }
            }
        });


        lineChart.invalidate(); // odświeżenie wykresu
    }
**/
/**
   private void updateLineChart(List<Expense> expenses) {
       SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
       Map<String, Float> dailySums = new TreeMap<>();

       for (Expense e : expenses) {
           try {
               Date date = sdf.parse(e.getDate());
               if (date == null) continue;
               String dayKey = sdf.format(date);

               float amount = (float) e.getAmount();
               dailySums.put(dayKey, dailySums.getOrDefault(dayKey, 0f) + amount);
           } catch (Exception ex) {
               ex.printStackTrace();
           }
       }

       float total = 0f;
       for (float value : dailySums.values()) {
           total += value;
       }
       textTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));

       List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
       List<String> labels = new ArrayList<>();

       int index = 0;
       for (Map.Entry<String, Float> entry : dailySums.entrySet()) {
           entries.add(new com.github.mikephil.charting.data.Entry(index, entry.getValue()));
           labels.add(entry.getKey());
           index++;
       }

       com.github.mikephil.charting.data.LineDataSet dataSet = new com.github.mikephil.charting.data.LineDataSet(entries, "Daily Expenses");
       dataSet.setColor(Color.parseColor("#63B1A1"));
       dataSet.setCircleColor(Color.parseColor("#63B1A1"));
       dataSet.setLineWidth(2f);
       dataSet.setCircleRadius(4f);
       dataSet.setValueTextSize(10f);

       com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(dataSet);
       lineChart.setData(lineData);

       // 🔹 Formatter dla osi X
       com.github.mikephil.charting.components.XAxis xAxis = lineChart.getXAxis();
       xAxis.setGranularity(1f);

       com.github.mikephil.charting.formatter.ValueFormatter xAxisFormatter = new com.github.mikephil.charting.formatter.ValueFormatter() {
           @Override
           public String getFormattedValue(float value) {
               int i = Math.round(value);
               if (i >= 0 && i < labels.size()) {
                   return labels.get(i);
               } else {
                   return "";
               }
           }
       };

       xAxis.setValueFormatter(xAxisFormatter);

       // 🔹 Tworzymy marker z tym samym formatterem
       ExpenseMarkerView marker = new ExpenseMarkerView(
               requireContext(),
               R.layout.marker_expense,
               xAxisFormatter
       );
       lineChart.setMarker(marker);

       lineChart.invalidate();
   }
}**/