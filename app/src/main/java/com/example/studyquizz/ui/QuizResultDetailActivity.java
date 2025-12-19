package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studyquizz.MainActivity;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityQuizResultDetailBinding;
import com.example.studyquizz.model.Quiz;
import com.example.studyquizz.model.QuizResult;
import com.example.studyquizz.ui.adapter.QuestionDetailAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizResultDetailActivity extends AppCompatActivity {
    public static final String EXTRA_QUIZ_ID = "quiz_id";
    public static final String EXTRA_TIMESTAMP = "timestamp";
    
    private ActivityQuizResultDetailBinding binding;
    private QuizRepository repository;
    private QuizResult result;
    private Quiz quiz;
    private QuestionDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set status bar to transparent
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        binding = ActivityQuizResultDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Adjust button container to avoid system navigation bar overlap
        binding.getRoot().setOnApplyWindowInsetsListener((v, insets) -> {
            int systemNavBarHeight = insets.getSystemWindowInsetBottom();
            int statusBarHeight = insets.getSystemWindowInsetTop();
            if (systemNavBarHeight > 0) {
                int extraPadding = 8; // Extra padding to ensure buttons are not covered
                binding.buttonContainer.setPadding(
                    binding.buttonContainer.getPaddingStart(),
                    binding.buttonContainer.getPaddingTop(),
                    binding.buttonContainer.getPaddingEnd(),
                    systemNavBarHeight + extraPadding
                );
            }
            return insets;
        });

        repository = QuizRepository.getInstance(this);
        
        // Get quiz ID and timestamp from intent
        String quizId = getIntent().getStringExtra(EXTRA_QUIZ_ID);
        long timestamp = getIntent().getLongExtra(EXTRA_TIMESTAMP, -1);
        
        if (quizId == null || timestamp == -1) {
            finish();
            return;
        }
        
        // Find the specific result from history
        List<QuizResult> results = repository.getHistoryForQuiz(quizId);
        result = null;
        for (QuizResult r : results) {
            if (r.getTimestamp() == timestamp) {
                result = r;
                break;
            }
        }
        
        if (result == null) {
            finish();
            return;
        }
        
        // Get quiz data
        quiz = repository.getQuizById(result.getQuizId());
        if (quiz == null) {
            finish();
            return;
        }
        
        setupViews();
        loadData();
    }

    private void setupViews() {
        // Set title
        binding.txtTitle.setText(result.getQuizTitle());
        
        // Format duration
        long duration = result.getDuration();
        String durationText = formatDuration(duration);
        binding.txtCompletionTime.setText("Thời gian hoàn thành: " + durationText);
        
        // Calculate and display percentage
        float percentage = 0f;
        if (result.getTotal() > 0) {
            percentage = (result.getCorrectCount() * 100f) / result.getTotal();
        }
        binding.txtPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percentage));
        binding.progressBar.setProgress((int) percentage);
        
        // Display correct count
        binding.txtCorrectCount.setText("Số câu đúng: " + result.getCorrectCount() + "/" + result.getTotal());
        
        // Setup RecyclerView for questions
        binding.recyclerQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionDetailAdapter();
        binding.recyclerQuestions.setAdapter(adapter);
        
        // Retry button
        binding.btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizPlayActivity.class);
            intent.putExtra(QuizPlayActivity.EXTRA_QUIZ_ID, quiz.getId());
            intent.putExtra(QuizPlayActivity.EXTRA_MODE, QuizPlayActivity.MODE_EXAM);
            startActivity(intent);
            finish();
        });
        
        // Return to setup button
        binding.btnReturnToSetup.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private String formatDuration(long durationMs) {
        if (durationMs <= 0) {
            return "0 phút 0 giây";
        }
        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d phút %d giây", minutes, seconds);
    }

    private void loadData() {
        // Prepare question data with user answers
        List<QuestionDetailAdapter.QuestionDetailItem> items = new ArrayList<>();
        List<Integer> userAnswers = result.getUserAnswers();
        
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            int userAnswer = i < userAnswers.size() ? userAnswers.get(i) : -1;
            items.add(new QuestionDetailAdapter.QuestionDetailItem(
                quiz.getQuestions().get(i),
                userAnswer,
                i + 1
            ));
        }
        
        adapter.submit(items);
    }
}

