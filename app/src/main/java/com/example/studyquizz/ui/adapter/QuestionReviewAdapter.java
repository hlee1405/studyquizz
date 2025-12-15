package com.example.studyquizz.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyquizz.databinding.ItemQuestionReviewBinding;
import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.QuestionType;

import java.util.ArrayList;
import java.util.List;

public class QuestionReviewAdapter extends RecyclerView.Adapter<QuestionReviewAdapter.QuestionReviewHolder> {

    public interface OnActionListener {
        void onRemove(Question question);
        void onQuestionUpdated(Question question, int position);
        void onCorrectAnswerChanged(Question question, int newCorrectIndex);
    }

    private final List<Question> items = new ArrayList<>();
    private final OnActionListener listener;

    public QuestionReviewAdapter(OnActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<Question> questions) {
        items.clear();
        if (questions != null) {
            items.addAll(questions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuestionReviewBinding binding = ItemQuestionReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new QuestionReviewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionReviewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class QuestionReviewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionReviewBinding binding;
        private boolean isEditMode = false;
        private Question currentQuestion;
        private int currentPosition;

        QuestionReviewHolder(ItemQuestionReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Question question, int position) {
            currentQuestion = question;
            currentPosition = position;
            isEditMode = false;
            
            // Question number
            binding.txtQuestionNumber.setText("Câu " + (position + 1) + ":");
            
            // Question content - remove "Câu X:" prefix if exists
            String content = question.getContent();
            // Remove pattern like "Câu 1:", "Câu 2:", etc. from the beginning
            content = content.replaceFirst("^Câu\\s*\\d+\\s*[:.]\\s*", "").trim();
            binding.txtQuestion.setText(content);
            binding.editQuestion.setText(content);
            
            // Show/hide options based on question type
            boolean isTrueFalse = question.getType() == QuestionType.TRUE_FALSE;
            binding.containerOptionC.setVisibility(isTrueFalse ? View.GONE : View.VISIBLE);
            binding.containerOptionD.setVisibility(isTrueFalse ? View.GONE : View.VISIBLE);
            
            // Display all options
            List<String> options = question.getOptions();
            if (options.size() > 0) {
                binding.txtOptionA.setText(options.get(0));
                binding.editOptionA.setText(options.get(0));
            }
            if (options.size() > 1) {
                binding.txtOptionB.setText(options.get(1));
                binding.editOptionB.setText(options.get(1));
            }
            if (options.size() > 2 && !isTrueFalse) {
                binding.txtOptionC.setText(options.get(2));
                binding.editOptionC.setText(options.get(2));
            }
            if (options.size() > 3 && !isTrueFalse) {
                binding.txtOptionD.setText(options.get(3));
                binding.editOptionD.setText(options.get(3));
            }
            
            // Set initial view mode
            setViewMode();
            
            // Highlight correct answer
            updateCorrectAnswerDisplay(question.getCorrectIndex());
            
            // Set click listeners for options to change correct answer
            binding.containerOptionA.setOnClickListener(v -> {
                if (listener != null && options.size() > 0) {
                    if (isEditMode) {
                        // In edit mode, just update display
                        currentQuestion.setCorrectIndex(0);
                        updateCorrectAnswerDisplay(0);
                    } else {
                        // In view mode, notify listener
                        listener.onCorrectAnswerChanged(question, 0);
                        updateCorrectAnswerDisplay(0);
                    }
                }
            });
            
            binding.containerOptionB.setOnClickListener(v -> {
                if (listener != null && options.size() > 1) {
                    if (isEditMode) {
                        currentQuestion.setCorrectIndex(1);
                        updateCorrectAnswerDisplay(1);
                    } else {
                        listener.onCorrectAnswerChanged(question, 1);
                        updateCorrectAnswerDisplay(1);
                    }
                }
            });
            
            if (!isTrueFalse) {
                binding.containerOptionC.setOnClickListener(v -> {
                    if (listener != null && options.size() > 2) {
                        if (isEditMode) {
                            currentQuestion.setCorrectIndex(2);
                            updateCorrectAnswerDisplay(2);
                        } else {
                            listener.onCorrectAnswerChanged(question, 2);
                            updateCorrectAnswerDisplay(2);
                        }
                    }
                });
                
                binding.containerOptionD.setOnClickListener(v -> {
                    if (listener != null && options.size() > 3) {
                        if (isEditMode) {
                            currentQuestion.setCorrectIndex(3);
                            updateCorrectAnswerDisplay(3);
                        } else {
                            listener.onCorrectAnswerChanged(question, 3);
                            updateCorrectAnswerDisplay(3);
                        }
                    }
                });
            }
            
            // Edit button - toggle edit mode
            binding.btnEdit.setOnClickListener(v -> {
                if (!isEditMode) {
                    setEditMode();
                }
            });
            
            // Save button
            binding.btnSave.setOnClickListener(v -> {
                saveQuestion();
            });
            
            // Delete button
            binding.btnDelete.setOnClickListener(v -> {
                if (!isEditMode && listener != null) {
                    listener.onRemove(question);
                }
            });
        }
        
        private void setViewMode() {
            isEditMode = false;
            // Show TextView, hide EditText
            binding.txtQuestion.setVisibility(View.VISIBLE);
            binding.editQuestion.setVisibility(View.GONE);
            binding.txtOptionA.setVisibility(View.VISIBLE);
            binding.editOptionA.setVisibility(View.GONE);
            binding.txtOptionB.setVisibility(View.VISIBLE);
            binding.editOptionB.setVisibility(View.GONE);
            binding.txtOptionC.setVisibility(View.VISIBLE);
            binding.editOptionC.setVisibility(View.GONE);
            binding.txtOptionD.setVisibility(View.VISIBLE);
            binding.editOptionD.setVisibility(View.GONE);
            
            // Show Edit button, hide Save button
            binding.btnEdit.setVisibility(View.VISIBLE);
            binding.btnSave.setVisibility(View.GONE);
        }
        
        private void setEditMode() {
            isEditMode = true;
            // Hide TextView, show EditText
            binding.txtQuestion.setVisibility(View.GONE);
            binding.editQuestion.setVisibility(View.VISIBLE);
            binding.txtOptionA.setVisibility(View.GONE);
            binding.editOptionA.setVisibility(View.VISIBLE);
            binding.txtOptionB.setVisibility(View.GONE);
            binding.editOptionB.setVisibility(View.VISIBLE);
            binding.txtOptionC.setVisibility(View.GONE);
            binding.editOptionC.setVisibility(View.VISIBLE);
            binding.txtOptionD.setVisibility(View.GONE);
            binding.editOptionD.setVisibility(View.VISIBLE);
            
            // Hide Edit button, show Save button
            binding.btnEdit.setVisibility(View.GONE);
            binding.btnSave.setVisibility(View.VISIBLE);
        }
        
        private void saveQuestion() {
            // Get updated content
            String newContent = binding.editQuestion.getText().toString().trim();
            if (newContent.isEmpty()) {
                return;
            }
            
            // Get updated options
            List<String> newOptions = new java.util.ArrayList<>();
            newOptions.add(binding.editOptionA.getText().toString().trim());
            newOptions.add(binding.editOptionB.getText().toString().trim());
            
            boolean isTrueFalse = currentQuestion.getType() == QuestionType.TRUE_FALSE;
            if (!isTrueFalse) {
                newOptions.add(binding.editOptionC.getText().toString().trim());
                newOptions.add(binding.editOptionD.getText().toString().trim());
            }
            
            // Remove empty options
            newOptions.removeIf(String::isEmpty);
            if (newOptions.size() < 2) {
                return;
            }
            
            // Update question
            currentQuestion.setContent(newContent);
            currentQuestion.setOptions(newOptions);
            
            // Ensure correct index is valid
            if (currentQuestion.getCorrectIndex() >= newOptions.size()) {
                currentQuestion.setCorrectIndex(0);
            }
            
            // Update display
            binding.txtQuestion.setText(newContent);
            binding.txtOptionA.setText(newOptions.get(0));
            binding.txtOptionB.setText(newOptions.get(1));
            if (!isTrueFalse && newOptions.size() > 2) {
                binding.txtOptionC.setText(newOptions.get(2));
            }
            if (!isTrueFalse && newOptions.size() > 3) {
                binding.txtOptionD.setText(newOptions.get(3));
            }
            
            // Notify listener
            if (listener != null) {
                listener.onQuestionUpdated(currentQuestion, currentPosition);
            }
            
            // Switch back to view mode
            setViewMode();
            updateCorrectAnswerDisplay(currentQuestion.getCorrectIndex());
        }
        
        private void updateCorrectAnswerDisplay(int correctIndex) {
            // Hide all checkmarks
            binding.iconCorrectA.setVisibility(View.GONE);
            binding.iconCorrectB.setVisibility(View.GONE);
            binding.iconCorrectC.setVisibility(View.GONE);
            binding.iconCorrectD.setVisibility(View.GONE);
            
            // Reset all backgrounds
            binding.containerOptionA.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_background);
            binding.containerOptionB.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_background);
            binding.containerOptionC.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_background);
            binding.containerOptionD.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_background);
            
            // Show checkmark and highlight correct answer
            switch (correctIndex) {
                case 0:
                    binding.iconCorrectA.setVisibility(View.VISIBLE);
                    binding.containerOptionA.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_selected_green);
                    break;
                case 1:
                    binding.iconCorrectB.setVisibility(View.VISIBLE);
                    binding.containerOptionB.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_selected_green);
                    break;
                case 2:
                    binding.iconCorrectC.setVisibility(View.VISIBLE);
                    binding.containerOptionC.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_selected_green);
                    break;
                case 3:
                    binding.iconCorrectD.setVisibility(View.VISIBLE);
                    binding.containerOptionD.setBackgroundResource(com.example.studyquizz.R.drawable.answer_field_selected_green);
                    break;
            }
        }
    }
}

