package com.example.expensetracker.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.expensetracker.R;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.ExpenseService.TimeFilter;
import com.example.expensetracker.utils.ExpenseMarkerView;
import com.example.expensetracker.viewmodel.StatsViewModel;
import com.example.expensetracker.viewmodel.StatsViewModelFactory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class StatsFragment extends Fragment {

    private StatsViewModel viewModel;

    private MaterialButton btnToday, btnYesterday, btnWeek, btnMonth, btnYear;
    private LineChart lineChart;
    private TextView textTotal;

    private static final String ACTIVE_COLOR = "#63B1A1";
    private static final String TEXT_ACTIVE = "#FFFFFF";
    private static final String TEXT_INACTIVE = "#63B1A1";

    private List<Expense> currentExpenses = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Initialize repository and service
        ExpenseRepository repository = new ExpenseRepository();
        ExpenseService expenseService = new ExpenseService();

        // Create factory with dependencies
        StatsViewModelFactory factory = new StatsViewModelFactory(repository, expenseService);

        // Create ViewModel using factory
        viewModel = new ViewModelProvider(this, factory).get(StatsViewModel.class);

        initViews(view);
        setupLineChart();
        setupFilterButtons();
        observeViewModel();

        return view;
    }

    private void initViews(View view) {
        lineChart = view.findViewById(R.id.line_chart);
        textTotal = view.findViewById(R.id.text_total);
        btnToday = view.findViewById(R.id.btn_today);
        btnYesterday = view.findViewById(R.id.btn_yesterday);
        btnWeek = view.findViewById(R.id.btn_week);
        btnMonth = view.findViewById(R.id.btn_month);
        btnYear = view.findViewById(R.id.btn_year);
    }


    private void setupFilterButtons() {
        List<MaterialButton> buttons = new ArrayList<>();
        buttons.add(btnToday);
        buttons.add(btnYesterday);
        buttons.add(btnWeek);
        buttons.add(btnMonth);
        buttons.add(btnYear);

        btnToday.setOnClickListener(v -> onFilterButtonClicked(TimeFilter.TODAY, buttons));
        btnYesterday.setOnClickListener(v -> onFilterButtonClicked(TimeFilter.YESTERDAY, buttons));
        btnWeek.setOnClickListener(v -> onFilterButtonClicked(TimeFilter.WEEK, buttons));
        btnMonth.setOnClickListener(v -> onFilterButtonClicked(TimeFilter.MONTH, buttons));
        btnYear.setOnClickListener(v -> onFilterButtonClicked(TimeFilter.YEAR, buttons));

        updateButtonStates(btnToday, buttons);
    }


    private void onFilterButtonClicked(TimeFilter timeFilter, List<MaterialButton> buttons) {
        viewModel.setTimeFilter(timeFilter);

        // Znajdź który przycisk odpowiada temu filtrowi
        MaterialButton activeButton = getButtonForFilter(timeFilter);
        updateButtonStates(activeButton, buttons);
    }


    private MaterialButton getButtonForFilter(TimeFilter timeFilter) {
        switch (timeFilter) {
            case TODAY: return btnToday;
            case YESTERDAY: return btnYesterday;
            case WEEK: return btnWeek;
            case MONTH: return btnMonth;
            case YEAR: return btnYear;
            default: return btnToday;
        }
    }


    private void updateButtonStates(MaterialButton activeButton, List<MaterialButton> allButtons) {
        for (MaterialButton button : allButtons) {
            if (button == activeButton) {
                button.setBackgroundColor(Color.parseColor(ACTIVE_COLOR));
                button.setTextColor(Color.parseColor(TEXT_ACTIVE));
            } else {
                button.setBackgroundColor(Color.TRANSPARENT);
                button.setTextColor(Color.parseColor(TEXT_INACTIVE));
            }
        }
    }


    private void observeViewModel() {
        viewModel.getFilteredExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null) {
                currentExpenses = expenses;
            }
        });

        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                textTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
            }
        });

        viewModel.getDailyGroupedExpenses().observe(getViewLifecycleOwner(), dailyData -> {
            if (dailyData != null) {
                updateLineChart(dailyData);
            }
        });
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getLegend().setEnabled(false);
    }

    private void updateLineChart(Map<String, Float> dailySums) {
        if (dailySums == null || dailySums.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Float> entry : dailySums.entrySet()) {
            entries.add(new Entry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Daily Expenses");
        dataSet.setColor(Color.parseColor(ACTIVE_COLOR));
        dataSet.setCircleColor(Color.parseColor(ACTIVE_COLOR));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = Math.round(value);
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                }
                return "";
            }
        });

        if (getContext() != null) {
            ExpenseMarkerView marker = new ExpenseMarkerView(
                    requireContext(),
                    R.layout.marker_expense,
                    currentExpenses
            );
            lineChart.setMarker(marker);
        }

        lineChart.animateX(500);
        lineChart.invalidate();
    }
}