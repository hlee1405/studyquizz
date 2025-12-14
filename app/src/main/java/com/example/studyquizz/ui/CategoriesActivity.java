package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityCategoriesBinding;
import com.example.studyquizz.ui.adapter.CustomCategoryAdapter;

import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private ActivityCategoriesBinding binding;
    private QuizRepository repository;
    private CustomCategoryAdapter customCategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set status bar to transparent and light icons
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = QuizRepository.getInstance(this);
        setupViews();
        loadCustomCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomCategories();
    }

    private void setupViews() {
        // Back button - return to MainActivity
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

        // Category cards - filter quizzes by category
        binding.cardScience.setOnClickListener(v -> filterAndReturn("Khoa học"));
        binding.cardGeography.setOnClickListener(v -> filterAndReturn("Địa lý"));
        binding.cardSports.setOnClickListener(v -> filterAndReturn("Thể thao"));
        binding.cardBiology.setOnClickListener(v -> filterAndReturn("Sinh học"));
        binding.cardTechnology.setOnClickListener(v -> filterAndReturn("Công nghệ"));
        binding.cardNetworking.setOnClickListener(v -> filterAndReturn("Mạng máy tính"));
        binding.cardSolarSystem.setOnClickListener(v -> filterAndReturn("Hệ mặt trời"));
        binding.cardTravel.setOnClickListener(v -> filterAndReturn("Du lịch"));

        // Setup RecyclerView for custom categories
        binding.recyclerCustomCategories.setLayoutManager(new GridLayoutManager(this, 2));
        customCategoryAdapter = new CustomCategoryAdapter(this::filterAndReturn);
        binding.recyclerCustomCategories.setAdapter(customCategoryAdapter);
    }

    private void loadCustomCategories() {
        List<String> customCategories = repository.getCustomCategories();
        if (customCategories != null && !customCategories.isEmpty()) {
            binding.lblCustomCategories.setVisibility(View.VISIBLE);
            binding.recyclerCustomCategories.setVisibility(View.VISIBLE);
            customCategoryAdapter.submit(customCategories);
        } else {
            binding.lblCustomCategories.setVisibility(View.GONE);
            binding.recyclerCustomCategories.setVisibility(View.GONE);
        }
    }

    private void filterAndReturn(String category) {
        Intent intent = new Intent();
        intent.putExtra("category", category);
        setResult(RESULT_OK, intent);
        finish();
    }
}

