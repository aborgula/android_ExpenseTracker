/**package com.example.expensetracker;

import android.content.Context;
import android.widget.TextView;

import com.example.expensetracker.Expense;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.example.expensetracker.R;
import java.util.List;

public class ExpenseMarkerView extends MarkerView {

    private TextView tvDate, tvAmount, tvName;
    private List<Expense> expenses;

    public ExpenseMarkerView(Context context, int layoutResource, List<Expense> expenses) {
        super(context, layoutResource);
        tvDate = findViewById(R.id.tvMarkerDate);
        tvAmount = findViewById(R.id.tvMarkerAmount);
        tvName = findViewById(R.id.tvMarkerName);
        this.expenses = expenses;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX(); // zakładamy, że Entry.x = index w liście
        if (index >= 0 && index < expenses.size()) {
            Expense expense = expenses.get(index);
            tvDate.setText("Date: " + expense.getDate());
            tvAmount.setText("Amount: $" + String.format("%.2f", expense.getAmount()));
            tvName.setText("Name: " + expense.getName());
        }
        super.refreshContent(e, highlight);
    }

    public int getXOffset(float xpos) {
        return -(getWidth() / 2);
    }

    public int getYOffset(float ypos) {
        return -getHeight();
    }
}**/


package com.example.expensetracker;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Locale;

public class ExpenseMarkerView extends MarkerView {

    private TextView tvDate, tvAmount;
    private ValueFormatter xAxisFormatter;

    public ExpenseMarkerView(Context context, int layoutResource, ValueFormatter xAxisFormatter) {
        super(context, layoutResource);
        tvDate = findViewById(R.id.tvMarkerDate);
        tvAmount = findViewById(R.id.tvMarkerAmount);
        this.xAxisFormatter = xAxisFormatter;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e == null) return;

        // Pobierz datę z formattera osi X
        String date = xAxisFormatter.getFormattedValue(e.getX());
        float amount = e.getY();

        tvDate.setText("Date: " + date);
        tvAmount.setText(String.format(Locale.getDefault(), "Amount: $%.2f", amount));

        super.refreshContent(e, highlight);
    }
/**
    @Override
    public int getXOffset(float xpos) {
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        return -getHeight();
    }**/
}