package com.example.expensetracker.model;

public class Expense {
    private String id;
    private String name;
    private String date;
    private double amount;
    private String category;
    private int categoryIcon;
    private String userId;

    public Expense() {}

    public Expense(String id, String name, String date, double amount, String category, int categoryIcon, String userId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.categoryIcon = categoryIcon;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getUserId() { return userId; }
}
