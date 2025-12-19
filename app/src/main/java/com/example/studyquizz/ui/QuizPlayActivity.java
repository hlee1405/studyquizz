package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studyquizz.R;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ActivityQuizPlayBinding;
import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.Quiz;
import com.example.studyquizz.model.QuizResult;
import com.example.studyquizz.ui.QuizResultActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizPlayActivity extends AppCompatActivity {
    public static final String EXTRA_QUIZ_ID = "quiz_id";
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_EXAM = "exam";
    public static final String MODE_STUDY = "study";

    private ActivityQuizPlayBinding binding;
    private Quiz quiz;
    private String mode = MODE_EXAM;
    private int position = 0;
    private List<Integer> answers;
    private CountDownTimer timer;
    private long timeLeft;
    private QuizRepository repository;
    private long startTime; // Thời gian bắt đầu làm quiz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizPlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = QuizRepository.getInstance(this);
        String quizId = getIntent().getStringExtra(EXTRA_QUIZ_ID);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        quiz = repository.getQuizById(quizId);
        if (quiz == null) {
            Toast.makeText(this, "Không tìm thấy quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (mode == null) mode = MODE_EXAM;
        answers = new ArrayList<>();
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            answers.add(-1);
        }

        setupControls();
        setupProgressIndicator();
        renderQuestion();
        
        // Lưu thời gian bắt đầu
        startTime = System.currentTimeMillis();
        
        if (MODE_EXAM.equals(mode)) {
            startTimer();
        } else {
            // Study mode: hide timer and clock icon
            binding.txtTimer.setVisibility(View.GONE);
            binding.imgTimer.setVisibility(View.GONE);
        }
    }
    
    /**
     * Setup progress indicator: segmented bar matching card width.
     * Each segment represents one question.
     * - Number of segments = total number of questions
     * - Segment width = (card width - gaps) / number of questions
     * - Segments are separated by small gaps (3dp) to create a "broken" appearance
     */
    private void setupProgressIndicator() {
        binding.progressIndicator.removeAllViews();
        int totalQuestions = quiz.getQuestions().size();
        
        if (totalQuestions == 0) return;
        
        // Wait for layout to measure, then get actual width
        binding.progressIndicator.post(() -> {
            // Get actual width from card to ensure exact match
            int totalWidthPx = binding.cardQuestion.getWidth();
            
            if (totalWidthPx <= 0) {
                // Fallback: use progress indicator width or calculate from screen
                totalWidthPx = binding.progressIndicator.getWidth();
                if (totalWidthPx <= 0) {
                    // Final fallback: calculate based on screen width minus margins (same as card)
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    float marginDp = 20f * 2; // 20dp start + 20dp end (same as card)
                    float marginPx = marginDp * getResources().getDisplayMetrics().density;
                    totalWidthPx = (int) (screenWidth - marginPx);
                }
            }
            
            // Gap between segments: 3dp
            float gapDp = 3f;
            float gapPx = gapDp * getResources().getDisplayMetrics().density;
            
            // Calculate total gap width
            float totalGapWidth = gapPx * (totalQuestions - 1);
            
            // Calculate segment width: (total width - total gaps) / N
            float availableWidth = totalWidthPx - totalGapWidth;
            float segmentWidthPx = availableWidth / totalQuestions;
            
            // Segment height: 6dp (increased for easier clicking)
            float segmentHeightDp = 6f;
            int segmentHeightPx = (int) (segmentHeightDp * getResources().getDisplayMetrics().density);
            
            // Corner radius: 2dp
            float cornerRadius = 2f * getResources().getDisplayMetrics().density;
            
            // Calculate total width used to ensure exact match
            float totalUsedWidth = 0;
            
            for (int i = 0; i < totalQuestions; i++) {
                // Create segment view
                View segment = new View(QuizPlayActivity.this);
                
                // For last segment, adjust width to fill remaining space exactly
                int segmentWidth;
                if (i == totalQuestions - 1) {
                    // Last segment: use remaining width to ensure total equals card width
                    segmentWidth = totalWidthPx - (int) totalUsedWidth;
                } else {
                    segmentWidth = (int) segmentWidthPx;
                    totalUsedWidth += segmentWidth + gapPx;
                }
                
                // Set width and height
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    segmentWidth, 
                    segmentHeightPx
                );
                
                // Add right margin (gap) except for the last segment
                if (i < totalQuestions - 1) {
                    params.setMargins(0, 0, (int) gapPx, 0);
                }
                
                segment.setLayoutParams(params);
                
                // Set rounded corners and default white color
                android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
                drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(cornerRadius);
                drawable.setColor(getResources().getColor(R.color.white)); // Default: white (not answered)
                drawable.setAlpha(255); // Full opacity
                segment.setBackground(drawable);
                
                // Store question index as tag
                final int questionIndex = i;
                segment.setTag(questionIndex);
                
                // Make segment clickable to navigate to corresponding question
                segment.setClickable(true);
                segment.setFocusable(true);
                segment.setOnClickListener(v -> {
                    // Save current answer before navigating
                    saveCurrentAnswer();
                    // Navigate to the clicked question
                    position = questionIndex;
                    renderQuestion();
                });
                
                binding.progressIndicator.addView(segment);
            }
            
            // Update colors based on current answers
            updateProgressIndicator();
        });
    }

    private void setupControls() {
        binding.btnNext.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (position < quiz.getQuestions().size() - 1) {
                position++;
                renderQuestion();
            } else {
                // Last question, show finish dialog
                showFinishDialog();
            }
        });
        binding.btnExit.setOnClickListener(v -> showExitDialog());
    }
    
    private void showFinishDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành quiz")
                .setMessage("Bạn có muốn nộp bài không?")
                .setPositiveButton("Nộp bài", (dialog, which) -> finishQuiz())
                .setNegativeButton("Tiếp tục", (dialog, which) -> {})
                .show();
    }
    
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát quiz")
                .setMessage("Bạn có chắc chắn muốn thoát? Tiến trình sẽ không được lưu.")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void renderQuestion() {
        Question question = quiz.getQuestions().get(position);
        binding.txtQuestionTitle.setText("Question " + (position + 1));
        binding.txtQuestionContent.setText(question.getContent());
        
        // Clear and rebuild answer options
        binding.containerAnswers.removeAllViews();
        for (int i = 0; i < question.getOptions().size(); i++) {
            View answerView = createAnswerView(question.getOptions().get(i), i, question, position);
            binding.containerAnswers.addView(answerView);
        }
        
        // Update progress indicator
        updateProgressIndicator();
        
        // Update Next button text
        if (position == quiz.getQuestions().size() - 1) {
            binding.btnNext.setText(getString(R.string.finish_text));
        } else {
            binding.btnNext.setText(getString(R.string.next_text));
        }
    }
    
    private View createAnswerView(String optionText, int index, Question question, int questionIndex) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, (int) (getResources().getDisplayMetrics().density * 12));
        container.setLayoutParams(params);
        container.setPadding(
            (int) (getResources().getDisplayMetrics().density * 16),
            (int) (getResources().getDisplayMetrics().density * 16),
            (int) (getResources().getDisplayMetrics().density * 16),
            (int) (getResources().getDisplayMetrics().density * 16)
        );
        container.setBackgroundResource(R.drawable.spinner_background);
        
        android.widget.TextView textView = new android.widget.TextView(this);
        textView.setText(optionText);
        textView.setTextColor(getResources().getColor(R.color.text_dark));
        textView.setTextSize(16);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        ));
        container.addView(textView);
        
        // Check if this is selected
        boolean isSelected = answers.get(questionIndex) == index;
        if (isSelected) {
            // In study mode, show green for correct, red for wrong
            if (MODE_STUDY.equals(mode)) {
                boolean isCorrect = index == question.getCorrectIndex();
                if (isCorrect) {
                    container.setBackgroundColor(getResources().getColor(R.color.join_quiz_blue));
                    textView.setTextColor(getResources().getColor(R.color.white));
                    
                    // Add checkmark icon
                    android.widget.ImageView checkIcon = new android.widget.ImageView(this);
                    checkIcon.setImageResource(R.drawable.ic_checkmark_white);
                    checkIcon.setLayoutParams(new LinearLayout.LayoutParams(
                        (int) (getResources().getDisplayMetrics().density * 24),
                        (int) (getResources().getDisplayMetrics().density * 24)
                    ));
                    container.addView(checkIcon);
                } else {
                    container.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    textView.setTextColor(getResources().getColor(R.color.white));
                    
                    // Add X icon
                    android.widget.ImageView xIcon = new android.widget.ImageView(this);
                    xIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                    xIcon.setColorFilter(getResources().getColor(R.color.white));
                    xIcon.setLayoutParams(new LinearLayout.LayoutParams(
                        (int) (getResources().getDisplayMetrics().density * 24),
                        (int) (getResources().getDisplayMetrics().density * 24)
                    ));
                    container.addView(xIcon);
                }
            } else {
                // Exam mode - just show selected
                container.setBackgroundColor(getResources().getColor(R.color.join_quiz_blue));
                textView.setTextColor(getResources().getColor(R.color.white));
                
                // Add checkmark icon
                android.widget.ImageView checkIcon = new android.widget.ImageView(this);
                checkIcon.setImageResource(R.drawable.ic_checkmark_white);
                checkIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (getResources().getDisplayMetrics().density * 24),
                    (int) (getResources().getDisplayMetrics().density * 24)
                ));
                container.addView(checkIcon);
            }
        }
        
        container.setOnClickListener(v -> {
            answers.set(questionIndex, index);
            // In study mode, no instant feedback toast - just update UI
            renderQuestion(); // Refresh to show selection
        });
        
        return container;
    }
    
    /**
     * Update progress indicator: change segment color based on mode and answer status.
     * - Study mode:
     *   - Correct answer: Green color
     *   - Wrong answer: Red color
     *   - Not answered: White color
     * - Exam mode:
     *   - Answered: Purple color (#9B59B6)
     *   - Not answered: White color
     */
    private void updateProgressIndicator() {
        int totalQuestions = quiz.getQuestions().size();
        int purpleColor = getResources().getColor(R.color.join_quiz_purple); // #9B59B6
        int greenColor = getResources().getColor(R.color.biology_green); // Green for correct
        int redColor = android.R.color.holo_red_dark; // Red for wrong
        
        for (int i = 0; i < totalQuestions; i++) {
            View segment = binding.progressIndicator.getChildAt(i);
            if (segment != null) {
                android.graphics.drawable.GradientDrawable drawable = 
                    (android.graphics.drawable.GradientDrawable) segment.getBackground();
                
                if (drawable != null) {
                    // Check if this question has been answered
                    boolean hasAnswer = answers.get(i) != -1;
                    
                    if (hasAnswer) {
                        if (MODE_STUDY.equals(mode)) {
                            // Study mode: check if answer is correct
                            Question question = quiz.getQuestions().get(i);
                            int selectedAnswer = answers.get(i);
                            boolean isCorrect = selectedAnswer == question.getCorrectIndex();
                            
                            if (isCorrect) {
                                // Correct answer: Green color
                                drawable.setColor(greenColor);
                            } else {
                                // Wrong answer: Red color
                                drawable.setColor(getResources().getColor(redColor));
                            }
                        } else {
                            // Exam mode: Purple color
                            drawable.setColor(purpleColor);
                        }
                        drawable.setAlpha(255);
                    } else {
                        // Not answered: White color
                        drawable.setColor(getResources().getColor(R.color.white));
                        drawable.setAlpha(255);
                    }
                }
            }
        }
    }

    private void showInstantFeedback(Question question, int checkedId) {
        // In study mode, show instant feedback
        // This can be implemented with a toast or dialog
        boolean correct = checkedId == question.getCorrectIndex();
        if (correct) {
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sai. Đáp án đúng: " + question.getOptions().get(question.getCorrectIndex()), 
                Toast.LENGTH_LONG).show();
        }
    }

    private void saveCurrentAnswer() {
        // Answer is already saved when user clicks on option
        // This method is kept for compatibility
    }

    private void startTimer() {
        // Sử dụng thời gian từ quiz (phút) chuyển sang milliseconds
        int durationMinutes = quiz.getDurationMinutes();
        if (durationMinutes <= 0) {
            // Fallback: nếu không có duration, dùng 1 phút mỗi câu hỏi
            durationMinutes = quiz.getQuestions().size();
        }
        timeLeft = durationMinutes * 60_000L;
        timer = new CountDownTimer(timeLeft, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                binding.txtTimer.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                finishQuiz();
            }
        }.start();
    }

    private void finishQuiz() {
        if (timer != null) timer.cancel();
        int correct = 0;
        int answered = 0;
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            Question q = quiz.getQuestions().get(i);
            int ans = answers.get(i);
            if (ans != -1) {
                answered++;
                if (ans == q.getCorrectIndex()) correct++;
            }
        }
        int total = quiz.getQuestions().size();
        int wrong = answered - correct;
        // Calculate score on scale of 10
        float scoreFloat = (correct * 10f) / total;
        int score = (int) (scoreFloat * 100); // Keep as percentage for QuizResult
        int completion = (int) ((answered * 100f) / total);
        
        // Tính thời gian làm bài thực tế
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        QuizResult result = new QuizResult(quiz.getId(), quiz.getTitle(), correct, wrong, total, score, completion, endTime, duration, new ArrayList<>(answers));
        repository.saveResult(result);

        // Navigate to result screen
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra(QuizResultActivity.EXTRA_SCORE, scoreFloat); // Pass as float for scale of 10
        intent.putExtra(QuizResultActivity.EXTRA_TOTAL, total);
        intent.putExtra(QuizResultActivity.EXTRA_CORRECT, correct);
        intent.putExtra(QuizResultActivity.EXTRA_INCORRECT, wrong);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}

