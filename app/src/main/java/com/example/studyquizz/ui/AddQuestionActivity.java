package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyquizz.R;
import com.example.studyquizz.databinding.ActivityAddQuestionBinding;
import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.QuestionType;

import java.util.ArrayList;
import java.util.List;

public class AddQuestionActivity extends AppCompatActivity {
    public static final String EXTRA_QUIZ_ID = "quiz_id";
    public static final String EXTRA_QUESTION_INDEX = "question_index";
    public static final String EXTRA_TOTAL_QUESTIONS = "total_questions";
    public static final String EXTRA_QUESTION_TYPE = "question_type";
    public static final String EXTRA_QUESTION_TO_EDIT = "question_to_edit";

    private ActivityAddQuestionBinding binding;
    private int questionIndex;
    private int totalQuestions;
    private boolean isMultipleChoice;
    private int selectedAnswerIndex = -1;
    private Question questionToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        questionIndex = getIntent().getIntExtra(EXTRA_QUESTION_INDEX, 1);
        totalQuestions = getIntent().getIntExtra(EXTRA_TOTAL_QUESTIONS, 1);
        isMultipleChoice = getIntent().getBooleanExtra(EXTRA_QUESTION_TYPE, true);
        questionToEdit = (Question) getIntent().getSerializableExtra(EXTRA_QUESTION_TO_EDIT);

        setupViews();
        setupProgressIndicator();
        
        // If editing, load question data
        if (questionToEdit != null) {
            loadQuestionForEdit();
        }
    }
    
    private void loadQuestionForEdit() {
        binding.inputQuestion.setText(questionToEdit.getContent());
        List<String> options = questionToEdit.getOptions();
        if (options.size() > 0) {
            binding.inputOption1.setText(options.get(0));
        }
        if (options.size() > 1) {
            binding.inputOption2.setText(options.get(1));
        }
        if (options.size() > 2) {
            binding.inputOption3.setText(options.get(2));
        }
        if (options.size() > 3) {
            binding.inputOption4.setText(options.get(3));
        }
        selectedAnswerIndex = questionToEdit.getCorrectIndex();
        updateAnswerIcons();
    }

    private void setupViews() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Set question number
        binding.txtQuestionNumber.setText(getString(R.string.question_number, questionIndex));

        // Setup question type (show/hide options 3 and 4)
        if (!isMultipleChoice) {
            // True/False mode
            binding.containerOption3.setVisibility(View.GONE);
            binding.containerOption4.setVisibility(View.GONE);
            binding.inputOption1.setText("Đúng");
            binding.inputOption2.setText("Sai");
            selectedAnswerIndex = 0;
            updateAnswerIcons();
        } else {
            binding.containerOption3.setVisibility(View.VISIBLE);
            binding.containerOption4.setVisibility(View.VISIBLE);
        }

        // Setup answer icon click listeners
        setupAnswerIcons();

        // Continue/Finish button
        if (questionIndex == totalQuestions) {
            binding.btnContinue.setText(R.string.finish_text);
        } else {
            binding.btnContinue.setText(R.string.continue_text);
        }
        binding.btnContinue.setOnClickListener(v -> handleContinue());
    }

    private void setupProgressIndicator() {
        binding.progressIndicator.removeAllViews();
        
        for (int i = 0; i < totalQuestions; i++) {
            View segment = new View(this);
            int width = (int) (getResources().getDisplayMetrics().density * 20);
            int height = (int) (getResources().getDisplayMetrics().density * 4);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(0, 0, (int) (getResources().getDisplayMetrics().density * 4), 0);
            segment.setLayoutParams(params);
            
            if (i < questionIndex) {
                // Filled segments (blue) - solid
                segment.setBackgroundColor(getResources().getColor(R.color.join_quiz_blue));
            } else {
                // Empty segments (light gray) - will appear as dashed
                segment.setBackgroundColor(getResources().getColor(R.color.text_gray));
                segment.setAlpha(0.3f);
            }
            
            binding.progressIndicator.addView(segment);
        }
    }

    private void setupAnswerIcons() {
        binding.iconCorrect1.setOnClickListener(v -> selectAnswer(0));
        binding.iconCorrect2.setOnClickListener(v -> selectAnswer(1));
        if (isMultipleChoice) {
            binding.iconCorrect3.setOnClickListener(v -> selectAnswer(2));
            binding.iconCorrect4.setOnClickListener(v -> selectAnswer(3));
        }
    }

    private void selectAnswer(int index) {
        selectedAnswerIndex = index;
        updateAnswerIcons();
    }

    private void updateAnswerIcons() {
        // Reset all icons
        binding.iconCorrect1.setImageResource(R.drawable.circle_empty);
        binding.iconCorrect2.setImageResource(R.drawable.circle_empty);
        if (isMultipleChoice) {
            binding.iconCorrect3.setImageResource(R.drawable.circle_empty);
            binding.iconCorrect4.setImageResource(R.drawable.circle_empty);
        }

        // Set selected icon
        if (selectedAnswerIndex == 0) {
            binding.iconCorrect1.setImageResource(R.drawable.circle_checkmark);
        } else if (selectedAnswerIndex == 1) {
            binding.iconCorrect2.setImageResource(R.drawable.circle_checkmark);
        } else if (selectedAnswerIndex == 2 && isMultipleChoice) {
            binding.iconCorrect3.setImageResource(R.drawable.circle_checkmark);
        } else if (selectedAnswerIndex == 3 && isMultipleChoice) {
            binding.iconCorrect4.setImageResource(R.drawable.circle_checkmark);
        }
    }

    private void handleContinue() {
        String content = binding.inputQuestion.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Nhập nội dung câu hỏi", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> options = new ArrayList<>();
        options.add(binding.inputOption1.getText().toString().trim());
        options.add(binding.inputOption2.getText().toString().trim());

        if (isMultipleChoice) {
            options.add(binding.inputOption3.getText().toString().trim());
            options.add(binding.inputOption4.getText().toString().trim());
        }

        if (selectedAnswerIndex == -1) {
            Toast.makeText(this, "Chọn đáp án đúng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int correctIndex = selectedAnswerIndex;

        List<String> cleaned = new ArrayList<>();
        for (String opt : options) {
            if (!TextUtils.isEmpty(opt)) {
                cleaned.add(opt);
            }
        }
        if (cleaned.size() < 2) {
            Toast.makeText(this, "Cần ít nhất 2 đáp án", Toast.LENGTH_SHORT).show();
            return;
        }

        if (correctIndex >= cleaned.size()) correctIndex = 0;

        QuestionType type = isMultipleChoice ? QuestionType.MULTIPLE_CHOICE : QuestionType.TRUE_FALSE;
        Question question = new Question(content, cleaned, correctIndex, type);

        // Return question to QuizBuilderActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("question", question);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}

