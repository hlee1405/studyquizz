package com.example.studyquizz.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studyquizz.R;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityQuizBuilderBinding;
import com.example.studyquizz.databinding.DialogAddQuestionBinding;
import com.example.studyquizz.databinding.DialogQuestionTypeBinding;
import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.QuestionType;
import com.example.studyquizz.model.Quiz;
import com.example.studyquizz.ui.adapter.QuestionSummaryAdapter;
import com.example.studyquizz.util.ImportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizBuilderActivity extends AppCompatActivity implements QuestionSummaryAdapter.OnActionListener {
    public static final String EXTRA_QUIZ_ID = "quiz_id";

    private ActivityQuizBuilderBinding binding;
    private QuizRepository repository;
    private Quiz currentQuiz;
    private QuestionSummaryAdapter adapter;
    private String generatedPassword;
    private String generatedQuizId;
    private int totalQuestions;
    private boolean isMultipleChoice;
    private int selectedQuestionTypeIndex = 0; // 0 = Multiple Choice, 1 = True/False

    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleImport);
    
    private final ActivityResultLauncher<Intent> addQuestionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Question question = (Question) result.getData().getSerializableExtra("question");
                    if (question != null) {
                        currentQuiz.addQuestion(question);
                        adapter.submit(currentQuiz.getQuestions());
                        
                        // Check if we've added enough questions
                        if (currentQuiz.getQuestions().size() >= totalQuestions) {
                            // Save quiz and finish
                            saveQuiz();
                        } else {
                            // Continue adding questions
                            int nextIndex = currentQuiz.getQuestions().size() + 1;
                            startAddQuestionActivity(nextIndex);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBuilderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = QuizRepository.getInstance(this);
        setupViews();
        loadQuizIfAny();
    }

    private void setupViews() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Generate Quiz ID and Password
        generateQuizIdAndPassword();

        // Setup Question Type Selector
        binding.layoutQuestionType.setOnClickListener(v -> showQuestionTypeDialog());
        updateQuestionTypeDisplay();

        // Setup Duration Input - default to 15 minutes
        binding.inputDuration.setText("15");

        // Edit Password button
        binding.btnEditPassword.setOnClickListener(v -> showEditPasswordDialog());

        // Continue button - will show dialog to add questions
        binding.btnContinue.setOnClickListener(v -> handleContinue());
    }

    private void showQuestionTypeDialog() {
        DialogQuestionTypeBinding dialogBinding = DialogQuestionTypeBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        // Set click listeners
        dialogBinding.btnMultipleChoice.setOnClickListener(v -> {
            selectedQuestionTypeIndex = 0;
            updateQuestionTypeDisplay();
            dialog.dismiss();
        });

        dialogBinding.btnTrueFalse.setOnClickListener(v -> {
            selectedQuestionTypeIndex = 1;
            updateQuestionTypeDisplay();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateQuestionTypeDisplay() {
        if (selectedQuestionTypeIndex == 0) {
            binding.txtQuestionType.setText(getString(R.string.multiple_choice_4));
        } else {
            binding.txtQuestionType.setText(getString(R.string.true_false));
        }
    }

    private void generateQuizIdAndPassword() {
        // Generate Quiz ID (format: XXX XXX XXXX)
        Random random = new Random();
        int part1 = 100 + random.nextInt(900); // 100-999
        int part2 = 100 + random.nextInt(900); // 100-999
        int part3 = 1000 + random.nextInt(9000); // 1000-9999
        generatedQuizId = part1 + " " + part2 + " " + part3;
        binding.txtQuizId.setText(generatedQuizId);

        // Generate Password (alphanumeric, 6 characters)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        generatedPassword = password.toString();
        binding.txtQuizPassword.setText(generatedPassword);
    }

    private void showEditPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(generatedPassword);
        input.setHint("Nhập mật khẩu mới");
        input.setPadding(50, 20, 50, 20);

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_password)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newPassword = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newPassword)) {
                        generatedPassword = newPassword;
                        binding.txtQuizPassword.setText(generatedPassword);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleContinue() {
        String title = binding.inputTitle.getText().toString().trim();
        String numberOfQuestionStr = binding.inputNumberOfQuestion.getText().toString().trim();
        String durationStr = binding.inputDuration.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Nhập tên quiz", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(numberOfQuestionStr)) {
            Toast.makeText(this, "Nhập số lượng câu hỏi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(durationStr)) {
            Toast.makeText(this, "Nhập thời gian làm bài", Toast.LENGTH_SHORT).show();
            return;
        }

        int numberOfQuestion;
        try {
            numberOfQuestion = Integer.parseInt(numberOfQuestionStr);
            if (numberOfQuestion <= 0) {
                Toast.makeText(this, "Số lượng câu hỏi phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng câu hỏi không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                Toast.makeText(this, "Thời gian làm bài phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Thời gian làm bài không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update quiz with basic info
        currentQuiz.setTitle(title);
        String description = "Thời gian: " + duration + " phút, Số câu hỏi: " + numberOfQuestion;
        currentQuiz.setDescription(description);
        currentQuiz.setDurationMinutes(duration); // Lưu thời gian vào quiz
        
        // Set default category (no category selection)
        currentQuiz.setCategory(null);
        currentQuiz.setCustomCategory(null);

        // Store question info
        totalQuestions = numberOfQuestion;
        isMultipleChoice = selectedQuestionTypeIndex == 0;

        // Start AddQuestionActivity
        startAddQuestionActivity(1);
    }
    
    private void startAddQuestionActivity(int questionIndex) {
        Intent intent = new Intent(this, AddQuestionActivity.class);
        intent.putExtra(AddQuestionActivity.EXTRA_QUIZ_ID, currentQuiz.getId());
        intent.putExtra(AddQuestionActivity.EXTRA_QUESTION_INDEX, questionIndex);
        intent.putExtra(AddQuestionActivity.EXTRA_TOTAL_QUESTIONS, totalQuestions);
        intent.putExtra(AddQuestionActivity.EXTRA_QUESTION_TYPE, isMultipleChoice);
        addQuestionLauncher.launch(intent);
    }

    private void loadQuizIfAny() {
        String quizId = getIntent().getStringExtra(EXTRA_QUIZ_ID);
        if (quizId != null) {
            currentQuiz = repository.getQuizById(quizId);
        }
        if (currentQuiz == null) {
            currentQuiz = new Quiz("", "", null, null);
        } else {
            binding.inputTitle.setText(currentQuiz.getTitle());
            // Load duration from quiz
            if (currentQuiz.getDurationMinutes() > 0) {
                binding.inputDuration.setText(String.valueOf(currentQuiz.getDurationMinutes()));
            }
            
            // Parse description if it contains duration info (fallback for old data)
            if (currentQuiz.getDescription() != null) {
                try {
                    String desc = currentQuiz.getDescription();
                    // Parse duration: "Thời gian: X phút, Số câu hỏi: Y"
                    if (desc.contains("Thời gian:") && desc.contains("phút")) {
                        String[] timeParts = desc.split("Thời gian: ");
                        if (timeParts.length > 1) {
                            String timeStr = timeParts[1].split(" phút")[0].trim();
                            binding.inputDuration.setText(timeStr);
                            // Also update quiz duration if not set
                            if (currentQuiz.getDurationMinutes() == 15) {
                                try {
                                    currentQuiz.setDurationMinutes(Integer.parseInt(timeStr));
                                } catch (NumberFormatException e) {
                                    // Ignore
                                }
                            }
                        }
                    }
                    // Parse number of questions
                    if (desc.contains("Số câu hỏi:")) {
                        String[] parts = desc.split("Số câu hỏi: ");
                        if (parts.length > 1) {
                            String numStr = parts[1].trim();
                            binding.inputNumberOfQuestion.setText(numStr);
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        // Initialize adapter for question list (hidden in new UI)
        adapter = new QuestionSummaryAdapter(this);
        binding.recyclerQuestions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerQuestions.setAdapter(adapter);
        adapter.submit(currentQuiz.getQuestions());
    }

    private void showAddQuestionDialog() {
        DialogAddQuestionBinding dialogBinding = DialogAddQuestionBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setTitle(R.string.add_question_title)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        // Set question type based on selection
        boolean isMultipleChoice = selectedQuestionTypeIndex == 0;
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, 
                new String[]{"Trắc nghiệm", "Đúng/Sai"});
        dialogBinding.spinnerType.setAdapter(typeAdapter);
        dialogBinding.spinnerType.setSelection(isMultipleChoice ? 0 : 1);
        dialogBinding.spinnerType.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> toggleTrueFalse(dialogBinding)));
        toggleTrueFalse(dialogBinding);
        connectRadioButtons(dialogBinding.radioCorrect1, dialogBinding.radioCorrect2, dialogBinding.radioCorrect3, dialogBinding.radioCorrect4);

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Question question = buildQuestionFromDialog(dialogBinding);
                if (question != null) {
                    currentQuiz.addQuestion(question);
                    adapter.submit(currentQuiz.getQuestions());
                    
                    // Check if we've added enough questions
                    int numberOfQuestion = Integer.parseInt(binding.inputNumberOfQuestion.getText().toString().trim());
                    if (currentQuiz.getQuestions().size() >= numberOfQuestion) {
                        // Save quiz and finish
                        saveQuiz();
                        dialog.dismiss();
                    } else {
                        // Continue adding questions
                        Toast.makeText(this, getString(R.string.questions_added, currentQuiz.getQuestions().size(), numberOfQuestion), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        showAddQuestionDialog(); // Show dialog again
                    }
                }
            });
        });

        dialog.show();
    }

    private void toggleTrueFalse(DialogAddQuestionBinding dialogBinding) {
        boolean isTrueFalse = dialogBinding.spinnerType.getSelectedItemPosition() == 1;
        dialogBinding.containerOption3.setVisibility(isTrueFalse ? View.GONE : View.VISIBLE);
        dialogBinding.containerOption4.setVisibility(isTrueFalse ? View.GONE : View.VISIBLE);
        if (isTrueFalse) {
            dialogBinding.inputOption1.setText("Đúng");
            dialogBinding.inputOption2.setText("Sai");
            dialogBinding.radioCorrect1.setChecked(true);
        }
    }

    private void connectRadioButtons(RadioButton... radios) {
        for (RadioButton rb : radios) {
            rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    for (RadioButton other : radios) {
                        if (other != buttonView) {
                            other.setChecked(false);
                        }
                    }
                }
            });
        }
    }

    private Question buildQuestionFromDialog(DialogAddQuestionBinding dialogBinding) {
        String content = dialogBinding.inputQuestion.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Nhập nội dung câu hỏi", Toast.LENGTH_SHORT).show();
            return null;
        }
        List<String> options = new ArrayList<>();
        List<RadioButton> radios = new ArrayList<>();
        options.add(dialogBinding.inputOption1.getText().toString().trim());
        options.add(dialogBinding.inputOption2.getText().toString().trim());
        radios.add(dialogBinding.radioCorrect1);
        radios.add(dialogBinding.radioCorrect2);

        boolean isTrueFalse = dialogBinding.spinnerType.getSelectedItemPosition() == 1;
        if (!isTrueFalse) {
            options.add(dialogBinding.inputOption3.getText().toString().trim());
            options.add(dialogBinding.inputOption4.getText().toString().trim());
            radios.add(dialogBinding.radioCorrect3);
            radios.add(dialogBinding.radioCorrect4);
        }

        int correctIndex = -1;
        for (int i = 0; i < radios.size(); i++) {
            if (radios.get(i).isChecked()) {
                correctIndex = i;
                break;
            }
        }
        if (correctIndex == -1) correctIndex = 0;

        List<String> cleaned = new ArrayList<>();
        for (String opt : options) {
            if (!TextUtils.isEmpty(opt)) {
                cleaned.add(opt);
            }
        }
        if (cleaned.size() < 2) {
            Toast.makeText(this, "Cần ít nhất 2 đáp án", Toast.LENGTH_SHORT).show();
            return null;
        }

        QuestionType type = isTrueFalse ? QuestionType.TRUE_FALSE : QuestionType.MULTIPLE_CHOICE;
        if (correctIndex >= cleaned.size()) correctIndex = 0;
        return new Question(content, cleaned, correctIndex, type);
    }

    private void handleImport(Uri uri) {
        if (uri == null) return;
        String lower = uri.toString().toLowerCase();
        List<Question> imported = new ArrayList<>();
        String mime = getContentResolver().getType(uri);
        if (mime != null && mime.contains("word")) {
            imported = ImportHelper.parseFromDocx(this, uri);
        } else if (mime != null && mime.contains("pdf")) {
            imported = ImportHelper.parseFromPdf(this, uri);
        } else if (lower.contains(".docx")) {
            imported = ImportHelper.parseFromDocx(this, uri);
        } else if (lower.contains(".pdf")) {
            imported = ImportHelper.parseFromPdf(this, uri);
        } else {
            imported = ImportHelper.parseFromPdf(this, uri);
        }
        if (!imported.isEmpty()) {
            currentQuiz.getQuestions().addAll(imported);
            adapter.submit(currentQuiz.getQuestions());
            Toast.makeText(this, "Đã import " + imported.size() + " câu hỏi", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Không đọc được dữ liệu câu hỏi", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQuiz() {
        if (repository.getQuizById(currentQuiz.getId()) == null) {
            repository.addQuiz(currentQuiz);
        } else {
            repository.updateQuiz(currentQuiz);
        }
        // Save Quiz ID and Password mapping
        repository.setQuizIdAndPassword(generatedQuizId, generatedPassword, currentQuiz.getId());
        showQuizCreatedScreen();
    }

    private void showQuizCreatedScreen() {
        Intent intent = new Intent(this, QuizCreatedActivity.class);
        intent.putExtra(QuizCreatedActivity.EXTRA_QUIZ_NAME, currentQuiz.getTitle());
        intent.putExtra(QuizCreatedActivity.EXTRA_QUIZ_ID, generatedQuizId);
        intent.putExtra(QuizCreatedActivity.EXTRA_PASSWORD, generatedPassword);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRemove(Question question) {
        currentQuiz.getQuestions().remove(question);
        adapter.submit(currentQuiz.getQuestions());
    }
}
