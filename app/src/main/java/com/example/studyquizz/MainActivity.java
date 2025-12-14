package com.example.studyquizz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studyquizz.data.AuthManager;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityMainBinding;
import com.example.studyquizz.model.Quiz;
import com.example.studyquizz.ui.CategoriesActivity;
import com.example.studyquizz.ui.HistoryActivity;
import com.example.studyquizz.ui.JoinQuizActivity;
import com.example.studyquizz.ui.LoginActivity;
import com.example.studyquizz.ui.MyQuizzesActivity;
import com.example.studyquizz.ui.ProfileActivity;
import com.example.studyquizz.ui.QuizBuilderActivity;
import com.example.studyquizz.ui.QuizPlayActivity;
import com.example.studyquizz.ui.adapter.QuizAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements QuizAdapter.OnQuizClickListener {

    private ActivityMainBinding binding;
    private QuizRepository repository;
    private AuthManager authManager;
    private QuizAdapter adapter;
    
    private final ActivityResultLauncher<Intent> categoriesLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String category = result.getData().getStringExtra("category");
                if (category != null) {
                    filterByCategory(category);
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set status bar to transparent and light icons
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Adjust bottom navigation bar to avoid system navigation bar overlap
        binding.getRoot().setOnApplyWindowInsetsListener((v, insets) -> {
            int systemNavBarHeight = insets.getSystemWindowInsetBottom();
            if (systemNavBarHeight > 0) {
                int extraPadding = 12; // Extra padding to ensure FAB is not covered
                binding.bottomNavLayout.setPadding(
                    binding.bottomNavLayout.getPaddingStart(),
                    binding.bottomNavLayout.getPaddingTop(),
                    binding.bottomNavLayout.getPaddingEnd(),
                    systemNavBarHeight + extraPadding
                );
            }
            return insets;
        });

        authManager = new AuthManager(this);
        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        repository = QuizRepository.getInstance(this);
        setupViews();
        loadData();
    }

    private void setupViews() {
        // Set user name from email (extract name part before @)
        String email = authManager.getEmail();
        String userName = email != null && email.contains("@") 
            ? email.substring(0, email.indexOf("@")) 
            : "Người dùng";
        // Capitalize first letter
        if (!userName.isEmpty()) {
            userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
        }
        binding.txtUserName.setText(userName);
        
        // Use LinearLayoutManager for horizontal list display
        binding.recyclerQuizzes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizAdapter(this);
        adapter.setRepository(repository);
        binding.recyclerQuizzes.setAdapter(adapter);

        // Quiz action cards
        binding.cardCreateQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizBuilderActivity.class)));
        binding.cardJoinQuiz.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, JoinQuizActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi mở màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // FAB for creating quiz
        binding.fabCreate.setOnClickListener(v -> startActivity(new Intent(this, QuizBuilderActivity.class)));

        // Bottom navigation
        binding.btnHome.setOnClickListener(v -> {
            // Already on home, do nothing or refresh
            loadData();
        });
        binding.btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });
        binding.btnTrophy.setOnClickListener(v -> {
            // Navigate to My Quizzes screen
            startActivity(new Intent(this, MyQuizzesActivity.class));
        });
        binding.btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });



        // Category cards - filter by category
        binding.cardScience.setOnClickListener(v -> filterByCategory("Khoa học"));
        binding.cardGeography.setOnClickListener(v -> filterByCategory("Địa lý"));
        binding.cardSports.setOnClickListener(v -> filterByCategory("Thể thao"));
        binding.cardBiology.setOnClickListener(v -> filterByCategory("Sinh học"));

        // See All Categories button - open MyQuizzesActivity
        if (binding.btnSeeAllCategories != null) {
            binding.btnSeeAllCategories.setOnClickListener(v -> {
                startActivity(new Intent(this, com.example.studyquizz.ui.MyQuizzesActivity.class));
            });
        }
    }

    private void showAllQuizzes() {
        // Hide categories grid
        binding.categoriesGrid.setVisibility(View.GONE);
        // Show quiz list section title
        binding.quizListSectionTitle.setVisibility(View.VISIBLE);
        // Show all quizzes
        List<Quiz> allQuizzes = repository.getQuizzes();
        adapter.submit(allQuizzes);
        binding.recyclerQuizzes.setVisibility(allQuizzes.isEmpty() ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(allQuizzes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void filterByCategory(String category) {
        // Hide categories grid
        binding.categoriesGrid.setVisibility(View.GONE);
        // Show quiz list section title
        binding.quizListSectionTitle.setVisibility(View.VISIBLE);
        
        List<Quiz> allQuizzes = repository.getQuizzes();
        List<Quiz> filteredQuizzes = new java.util.ArrayList<>();
        
        for (Quiz quiz : allQuizzes) {
            String quizCategory = quiz.getCustomCategory() != null && !quiz.getCustomCategory().isEmpty()
                    ? quiz.getCustomCategory() : quiz.getCategory();
            if (category.equals(quizCategory)) {
                filteredQuizzes.add(quiz);
            }
        }
        
        adapter.submit(filteredQuizzes);
        binding.recyclerQuizzes.setVisibility(filteredQuizzes.isEmpty() ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(filteredQuizzes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadData() {
        List<Quiz> quizzes = repository.getQuizzes();
        // Show only first 3 quizzes on main screen
        List<Quiz> limitedQuizzes = new java.util.ArrayList<>();
        for (int i = 0; i < Math.min(3, quizzes.size()); i++) {
            limitedQuizzes.add(quizzes.get(i));
        }
        adapter.submit(limitedQuizzes);
        // By default, show quiz list and hide categories grid
        binding.categoriesGrid.setVisibility(View.GONE);
        binding.quizListSectionTitle.setVisibility(View.VISIBLE);
        binding.recyclerQuizzes.setVisibility(limitedQuizzes.isEmpty() ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(limitedQuizzes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager.isLoggedIn()) {
            loadData();
        }
    }

    @Override
    public void onStartQuiz(Quiz quiz) {
        showModePicker(quiz);
    }

    @Override
    public void onEditQuiz(Quiz quiz) {
        Intent intent = new Intent(this, QuizBuilderActivity.class);
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
                    loadData();
                    Toast.makeText(this, "Đã xóa quiz", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showModePicker(Quiz quiz) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mode_picker, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        // Make dialog window transparent for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialogView.findViewById(R.id.btnExamMode).setOnClickListener(v -> {
            startPlay(quiz, QuizPlayActivity.MODE_EXAM);
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.btnStudyMode).setOnClickListener(v -> {
            startPlay(quiz, QuizPlayActivity.MODE_STUDY);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void startPlay(Quiz quiz, String mode) {
        if (quiz.getQuestions().isEmpty()) {
            Toast.makeText(this, R.string.no_question_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, QuizPlayActivity.class);
        intent.putExtra(QuizPlayActivity.EXTRA_QUIZ_ID, quiz.getId());
        intent.putExtra(QuizPlayActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }
}