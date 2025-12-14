package com.example.studyquizz.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyquizz.R;
import com.example.studyquizz.databinding.ItemCustomCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class CustomCategoryAdapter extends RecyclerView.Adapter<CustomCategoryAdapter.CustomCategoryHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    private final List<String> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;

    public CustomCategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<String> categoryList) {
        categories.clear();
        if (categoryList != null) {
            categories.addAll(categoryList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomCategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomCategoryBinding binding = ItemCustomCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomCategoryHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomCategoryHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CustomCategoryHolder extends RecyclerView.ViewHolder {
        private final ItemCustomCategoryBinding binding;

        CustomCategoryHolder(ItemCustomCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String categoryName) {
            binding.txtCategoryName.setText(categoryName);
            binding.txtCategoryName.setTextColor(binding.getRoot().getContext().getColor(R.color.join_quiz_blue));
            
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(categoryName);
                }
            });
        }
    }
}


