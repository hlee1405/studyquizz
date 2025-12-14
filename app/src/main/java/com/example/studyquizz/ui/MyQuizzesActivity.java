package com.example.studyquizz.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityMyQuizzesBinding;
import com.example.studyquizz.model.Quiz;
import com.example.studyquizz.ui.adapter.QuizAdapter;

import java.util.List;

public class MyQuizzesActivity extends AppCompatActivity implements QuizAdapter.OnQuizClickListener {

    private ActivityMyQuizzesBinding binding;
    private QuizRepository repository;
    private QuizAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set status bar to transparent and light icons
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        binding = ActivityMyQuizzesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = QuizRepository.getInstance(this);
        setupViews();
        loadQuizzes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuizzes();
    }

    private void setupViews() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView với tối ưu hiệu năng
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recyclerQuizzes.setLayoutManager(layoutManager);
        
        // Tối ưu: RecyclerView tự scroll, không cần nested scrolling
        binding.recyclerQuizzes.setHasFixedSize(false);
        
        // Tối ưu: Cache để cải thiện hiệu năng khi scroll
        binding.recyclerQuizzes.setItemViewCacheSize(20);
        binding.recyclerQuizzes.setDrawingCacheEnabled(true);
        
        adapter = new QuizAdapter(this);
        adapter.setRepository(repository);
        binding.recyclerQuizzes.setAdapter(adapter);
    }

    private void loadQuizzes() {
        List<Quiz> quizzes = repository.getQuizzes();
        adapter.submit(quizzes);
        binding.recyclerQuizzes.setVisibility(quizzes.isEmpty() ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(quizzes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStartQuiz(Quiz quiz) {
        // Show mode picker dialog
        android.view.View dialogView = android.view.LayoutInflater.from(this).inflate(com.example.studyquizz.R.layout.dialog_mode_picker, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        // Make dialog window transparent for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialogView.findViewById(com.example.studyquizz.R.id.btnExamMode).setOnClickListener(v -> {
            startPlay(quiz, QuizPlayActivity.MODE_EXAM);
            dialog.dismiss();
        });
        dialogView.findViewById(com.example.studyquizz.R.id.btnStudyMode).setOnClickListener(v -> {
            startPlay(quiz, QuizPlayActivity.MODE_STUDY);
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onEditQuiz(Quiz quiz) {
        android.content.Intent intent = new android.content.Intent(this, QuizBuilderActivity.class);
        intent.putExtra(QuizBuilderActivity.EXTRA_QUIZ_ID, quiz.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteQuiz(Quiz quiz) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa quiz \"" + quiz.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    repository.deleteQuiz(quiz.getId());
                    loadQuizzes();
                    android.widget.Toast.makeText(this, "Đã xóa quiz", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void startPlay(Quiz quiz, String mode) {
        if (quiz.getQuestions().isEmpty()) {
            android.widget.Toast.makeText(this, com.example.studyquizz.R.string.no_question_warning, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.Intent intent = new android.content.Intent(this, QuizPlayActivity.class);
        intent.putExtra(QuizPlayActivity.EXTRA_QUIZ_ID, quiz.getId());
        intent.putExtra(QuizPlayActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }
}

