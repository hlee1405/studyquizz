package com.example.studyquizz.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.studyquizz.R;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityQuizBuilderBinding;
import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.QuestionType;
import com.example.studyquizz.model.Quiz;
import com.example.studyquizz.ui.adapter.QuestionReviewAdapter;
import com.example.studyquizz.ui.adapter.QuestionSummaryAdapter;
import com.example.studyquizz.util.ImportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizBuilderActivity extends AppCompatActivity implements QuestionSummaryAdapter.OnActionListener, QuestionReviewAdapter.OnActionListener {
    public static final String EXTRA_QUIZ_ID = "quiz_id";

    private ActivityQuizBuilderBinding binding;
    private QuizRepository repository;
    private Quiz currentQuiz;
    private QuestionSummaryAdapter adapter;
    private QuestionReviewAdapter reviewAdapter;
    private String generatedPassword;
    private String generatedQuizId;
    private int totalQuestions;
    private boolean isMultipleChoice;
    private int selectedQuestionTypeIndex = 0; // 0 = Multiple Choice, 1 = True/False

    private final ActivityResultLauncher<String> filePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleImport);
    
    private int editingQuestionPosition = -1;
    
    private final ActivityResultLauncher<Intent> addQuestionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Question question = (Question) result.getData().getSerializableExtra("question");
                    if (question != null) {
                        if (editingQuestionPosition >= 0 && editingQuestionPosition < currentQuiz.getQuestions().size()) {
                            // Update existing question
                            currentQuiz.getQuestions().set(editingQuestionPosition, question);
                            editingQuestionPosition = -1;
                        } else {
                            // Add new question
                            currentQuiz.addQuestion(question);
                            
                            // Check if we've added enough questions
                            if (currentQuiz.getQuestions().size() >= totalQuestions) {
                                // Save quiz and finish
                                saveQuiz();
                                return;
                            } else {
                                // Continue adding questions
                                int nextIndex = currentQuiz.getQuestions().size() + 1;
                                startAddQuestionActivity(nextIndex);
                                return;
                            }
                        }
                        
                        // Update adapters
                        if (reviewAdapter != null) {
                            reviewAdapter.submit(currentQuiz.getQuestions());
                        }
                        if (adapter != null) {
                            adapter.submit(currentQuiz.getQuestions());
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

        // Import File button
        binding.btnImport.setOnClickListener(v -> {
            // Open file picker for PDF and DOCX files
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimeTypes = {
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword"
                };
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                Intent chooser = Intent.createChooser(intent, "Chọn file PDF hoặc DOCX");
                if (chooser.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(chooser, 100);
                } else {
                    Toast.makeText(this, "Không tìm thấy ứng dụng để chọn file", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi mở file picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // Continue button - will show dialog to add questions
        binding.btnContinue.setOnClickListener(v -> handleContinue());
    }

    private void showQuestionTypeDialog() {
        String[] options = new String[]{"Trắc nghiệm 4 đáp án", "Đúng/Sai"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn loại câu hỏi")
                .setItems(options, (dialog, which) -> {
                    selectedQuestionTypeIndex = which;
                    updateQuestionTypeDisplay();
                })
                .show();
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

        if (TextUtils.isEmpty(durationStr)) {
            Toast.makeText(this, "Nhập thời gian làm bài", Toast.LENGTH_SHORT).show();
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

        // Check if we have questions
        if (currentQuiz.getQuestions().isEmpty()) {
            // No questions - need to add questions manually
            if (TextUtils.isEmpty(numberOfQuestionStr)) {
                Toast.makeText(this, "Nhập số lượng câu hỏi", Toast.LENGTH_SHORT).show();
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

            // Update quiz with basic info
            currentQuiz.setTitle(title);
            String description = "Thời gian: " + duration + " phút, Số câu hỏi: " + numberOfQuestion;
            currentQuiz.setDescription(description);
            currentQuiz.setDurationMinutes(duration);
            
            // Set default category
            currentQuiz.setCategory(null);
            currentQuiz.setCustomCategory(null);

            // Store question info
            totalQuestions = numberOfQuestion;
            isMultipleChoice = selectedQuestionTypeIndex == 0;

            // Start AddQuestionActivity
            startAddQuestionActivity(1);
        } else {
            // We have questions (from import) - save quiz
            int numberOfQuestion = currentQuiz.getQuestions().size();
            
            // Update quiz with basic info
            currentQuiz.setTitle(title);
            String description = "Thời gian: " + duration + " phút, Số câu hỏi: " + numberOfQuestion;
            currentQuiz.setDescription(description);
            currentQuiz.setDurationMinutes(duration);
            
            // Set default category
            currentQuiz.setCategory(null);
            currentQuiz.setCustomCategory(null);
            
            // Save and finish
            saveQuiz();
        }
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
        // Initialize adapter for question list
        adapter = new QuestionSummaryAdapter(this);
        
        // Initialize review adapter
        reviewAdapter = new QuestionReviewAdapter(this);
        binding.recyclerQuestions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerQuestions.setAdapter(reviewAdapter);
        reviewAdapter.submit(currentQuiz.getQuestions());
        
        // Update button text based on state
        updateContinueButtonText();
    }

    private void updateContinueButtonText() {
        if (!currentQuiz.getQuestions().isEmpty()) {
            // If we have questions, button should say "Hoàn thành"
            binding.btnContinue.setText(getString(R.string.finish_text));
        } else {
            // If no questions, button should say "Tiếp tục"
            binding.btnContinue.setText(getString(R.string.continue_text));
        }
    }

    // Lấy tên file hiển thị từ Uri để show lên nút Import
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            android.database.Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        result = cursor.getString(nameIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
            if (result != null) {
                int slash = result.lastIndexOf('/');
                if (slash >= 0 && slash < result.length() - 1) {
                    result = result.substring(slash + 1);
                }
            }
        }
        return result;
    }


    private void handleImport(Uri uri) {
        if (uri == null) return;
        String fileName = getFileNameFromUri(uri);
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
            currentQuiz.getQuestions().clear(); // Clear existing questions
            currentQuiz.getQuestions().addAll(imported);
            reviewAdapter.submit(currentQuiz.getQuestions());
            
            // Update number of questions field with imported count
            int totalImported = currentQuiz.getQuestions().size();
            binding.inputNumberOfQuestion.setText(String.valueOf(totalImported));
            
            // Update import button text to show imported file name
            if (fileName != null && !fileName.isEmpty()) {
                binding.btnImport.setText(fileName);
            } else {
                binding.btnImport.setText(getString(R.string.import_file_done));
            }

            // Show review screen
            showReviewScreen();
            
            Toast.makeText(this, "Đã import " + imported.size() + " câu hỏi", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Không đọc được dữ liệu câu hỏi. Vui lòng kiểm tra định dạng file.", Toast.LENGTH_LONG).show();
        }
    }

    private void showReviewScreen() {
        // Hide form, show review screen
        binding.cardForm.setVisibility(View.GONE);
        binding.cardReviewQuestions.setVisibility(View.VISIBLE);
        
        // Setup continue button in review screen
        binding.btnContinueReview.setOnClickListener(v -> {
            // Go back to form
            binding.cardReviewQuestions.setVisibility(View.GONE);
            binding.cardForm.setVisibility(View.VISIBLE);
            // Update button text to "Hoàn thành" if we have questions
            updateContinueButtonText();
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                handleImport(uri);
            }
        }
    }

    @Override
    public void onRemove(Question question) {
        currentQuiz.getQuestions().remove(question);
        if (reviewAdapter != null) {
            reviewAdapter.submit(currentQuiz.getQuestions());
        }
        if (adapter != null) {
            adapter.submit(currentQuiz.getQuestions());
        }
        // Update number of questions
        binding.inputNumberOfQuestion.setText(String.valueOf(currentQuiz.getQuestions().size()));
    }

    // QuestionReviewAdapter callbacks
    @Override
    public void onQuestionUpdated(Question question, int position) {
        // Question was updated inline, just refresh adapter
        if (reviewAdapter != null) {
            reviewAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onCorrectAnswerChanged(Question question, int newCorrectIndex) {
        question.setCorrectIndex(newCorrectIndex);
        if (reviewAdapter != null) {
            reviewAdapter.notifyDataSetChanged();
        }
    }
}
