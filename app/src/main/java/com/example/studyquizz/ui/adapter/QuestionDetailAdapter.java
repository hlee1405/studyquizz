package com.example.studyquizz.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyquizz.R;
import com.example.studyquizz.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionDetailAdapter extends RecyclerView.Adapter<QuestionDetailAdapter.QuestionDetailHolder> {

    private final List<QuestionDetailItem> items = new ArrayList<>();

    public static class QuestionDetailItem {
        public Question question;
        public int userAnswer; // -1 if not answered, otherwise the selected index
        public int questionNumber;

        public QuestionDetailItem(Question question, int userAnswer, int questionNumber) {
            this.question = question;
            this.userAnswer = userAnswer;
            this.questionNumber = questionNumber;
        }
    }

    public void submit(List<QuestionDetailItem> items) {
        this.items.clear();
        if (items != null) {
            this.items.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_detail, parent, false);
        return new QuestionDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionDetailHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class QuestionDetailHolder extends RecyclerView.ViewHolder {
        private final TextView txtQuestionNumber;
        private final TextView txtQuestionContent;
        private final LinearLayout containerOptions;

        QuestionDetailHolder(View itemView) {
            super(itemView);
            txtQuestionNumber = itemView.findViewById(R.id.txtQuestionNumber);
            txtQuestionContent = itemView.findViewById(R.id.txtQuestionContent);
            containerOptions = itemView.findViewById(R.id.containerOptions);
        }

        void bind(QuestionDetailItem item) {
            Question question = item.question;
            int userAnswer = item.userAnswer;
            int questionNumber = item.questionNumber;

            // Set question number and content
            txtQuestionNumber.setText("Câu " + questionNumber);
            txtQuestionContent.setText(question.getContent());

            // Clear and rebuild options
            containerOptions.removeAllViews();
            
            int correctIndex = question.getCorrectIndex();
            List<String> options = question.getOptions();
            
            for (int i = 0; i < options.size(); i++) {
                View optionView = createOptionView(options.get(i), i, userAnswer, correctIndex);
                containerOptions.addView(optionView);
            }
        }

        private View createOptionView(String optionText, int index, int userAnswer, int correctIndex) {
            LinearLayout container = new LinearLayout(itemView.getContext());
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setGravity(android.view.Gravity.CENTER_VERTICAL);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, (int) (itemView.getContext().getResources().getDisplayMetrics().density * 12));
            container.setLayoutParams(params);
            container.setPadding(
                (int) (itemView.getContext().getResources().getDisplayMetrics().density * 16),
                (int) (itemView.getContext().getResources().getDisplayMetrics().density * 16),
                (int) (itemView.getContext().getResources().getDisplayMetrics().density * 16),
                (int) (itemView.getContext().getResources().getDisplayMetrics().density * 16)
            );
            
            // Default background
            container.setBackgroundResource(R.drawable.spinner_background);
            
            TextView textView = new TextView(itemView.getContext());
            textView.setText(optionText);
            textView.setTextColor(itemView.getContext().getResources().getColor(R.color.text_dark));
            textView.setTextSize(15);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ));
            container.addView(textView);
            
            // Determine if this option is correct or user's answer
            boolean isCorrect = index == correctIndex;
            boolean isUserAnswer = index == userAnswer;
            boolean userAnsweredWrong = userAnswer != -1 && userAnswer != correctIndex;
            
            if (isCorrect) {
                // Correct answer - always show green checkmark
                container.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.correct_green));
                textView.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
                
                // Add checkmark icon
                TextView checkIcon = new TextView(itemView.getContext());
                checkIcon.setText("✓");
                checkIcon.setTextSize(20);
                checkIcon.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
                checkIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                container.addView(checkIcon);
            } else if (isUserAnswer && userAnsweredWrong) {
                // User selected wrong answer - show red X
                container.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.wrong_red));
                textView.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
                
                // Add X icon
                TextView xIcon = new TextView(itemView.getContext());
                xIcon.setText("✗");
                xIcon.setTextSize(20);
                xIcon.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
                xIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                container.addView(xIcon);
            }
            
            return container;
        }
    }
}

