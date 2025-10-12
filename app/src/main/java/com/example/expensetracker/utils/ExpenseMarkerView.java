package com.example.expensetracker.utils;

import android.content.Context;
import android.widget.TextView;

import com.example.expensetracker.R;
import com.example.expensetracker.model.Expense;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
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
        int index = (int) e.getX();
        if (index >= 0 && index < expenses.size()) {
            Expense expense = expenses.get(index);
            tvDate.setText("Date: " + expense.getDate());
            tvAmount.setText("Amount: $" + String.format("%.2f", expense.getAmount()));
            tvName.setText("Name: " + expense.getName());
        }
        super.refreshContent(e, highlight);
    }
}