package com.example.expensetracker;

public class Expense {
    private String id; // 🔹 unikalny klucz w Realtime Database
    private String name;
    private String date;
    private double amount;
    private String category;
    private int categoryIcon;
    private String userId;

    // 🔸 konstruktor bezargumentowy wymagany przez Firebase
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

    // 🔹 getters i setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public int getCategoryIcon() { return categoryIcon; }
    public String getUserId() { return userId; }
}
