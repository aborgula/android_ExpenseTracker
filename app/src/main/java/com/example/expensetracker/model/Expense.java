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

    public Expense(String id, String name, String date, double amount,
                   String category, int categoryIcon, String userId) {
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
    public void setName(String name) { this.name = name; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(int categoryIcon) { this.categoryIcon = categoryIcon; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
