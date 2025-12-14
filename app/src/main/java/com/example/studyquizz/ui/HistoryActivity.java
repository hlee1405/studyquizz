package com.example.studyquizz.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityHistoryBinding;
import com.example.studyquizz.model.QuizResult;
import com.example.studyquizz.ui.adapter.HistoryAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;
    private HistoryAdapter adapter;
    private QuizRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = QuizRepository.getInstance(this);
        adapter = new HistoryAdapter();
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHistory.setAdapter(adapter);
        
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());
        
        loadData();
    }

    private void loadData() {
        Map<String, List<QuizResult>> map = repository.getAllHistory();
        List<QuizResult> combined = new ArrayList<>();
        for (List<QuizResult> results : map.values()) {
            combined.addAll(results);
        }
        combined.sort(Comparator.comparingLong(QuizResult::getTimestamp));
        Collections.reverse(combined);
        adapter.submit(combined);
        binding.emptyView.setVisibility(combined.isEmpty() ? View.VISIBLE : View.GONE);
    }
}




