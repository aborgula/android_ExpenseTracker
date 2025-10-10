package com.example.expensetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getName());

        // Przypisanie obrazka w zależności od nazwy kategorii
        switch (category.getName()) {
            case "Food":
                holder.categoryIcon.setImageResource(R.drawable.ic_food);
                break;
            case "Transport":
                holder.categoryIcon.setImageResource(R.drawable.ic_transport);
                break;
            case "Shopping":
                holder.categoryIcon.setImageResource(R.drawable.ic_shopping);
                break;
            case "Entertainment":
                holder.categoryIcon.setImageResource(R.drawable.ic_entertainment);
                break;
            case "Health":
                holder.categoryIcon.setImageResource(R.drawable.ic_health);
                break;
            case "Bills":
                holder.categoryIcon.setImageResource(R.drawable.ic_bills);
                break;
            case "Other":
                holder.categoryIcon.setImageResource(R.drawable.ic_other);
                break;
            default:
                holder.categoryIcon.setImageResource(R.drawable.ic_other);
        }

        // Obsługa kliknięcia
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    // ViewHolder
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
        }
    }
}