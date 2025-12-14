package com.example.studyquizz.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyquizz.R;
import com.example.studyquizz.data.QuizRepository;
import com.example.studyquizz.databinding.ItemQuizBinding;
import com.example.studyquizz.model.Quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizHolder> {

    public interface OnQuizClickListener {
        void onStartQuiz(Quiz quiz);

        void onEditQuiz(Quiz quiz);

        void onDeleteQuiz(Quiz quiz);
    }

    private final List<Quiz> items = new ArrayList<>();
    private final OnQuizClickListener listener;
    private QuizRepository repository;

    public QuizAdapter(OnQuizClickListener listener) {
        this.listener = listener;
    }
    
    public void setRepository(QuizRepository repository) {
        this.repository = repository;
    }

    public void submit(List<Quiz> quizzes) {
        items.clear();
        if (quizzes != null) {
            items.addAll(quizzes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuizBinding binding = ItemQuizBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new QuizHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class QuizHolder extends RecyclerView.ViewHolder {
        private final ItemQuizBinding binding;

        QuizHolder(ItemQuizBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Quiz quiz) {
            binding.txtTitle.setText(quiz.getTitle());
            
            // Set category icon based on category
            String category = quiz.getCustomCategory() != null && !quiz.getCustomCategory().isEmpty()
                    ? quiz.getCustomCategory() : quiz.getCategory();
            setCategoryIcon(category);
            
            // Set text color based on category
            setCategoryTextColor(category);
            
            // Set number of questions
            int questionCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
            
            // Set number of plays
            int playCount = 0;
            if (repository != null) {
                playCount = repository.getHistoryForQuiz(quiz.getId()).size();
            }
            
            // Display info: "X câu hỏi - Y lượt chơi"
            binding.txtInfo.setText(questionCount + " câu hỏi - " + playCount + " lượt chơi");

            // Click to start quiz
            binding.getRoot().setOnClickListener(v -> listener.onStartQuiz(quiz));
            
            // Long press to show popup menu
            binding.getRoot().setOnLongClickListener(v -> {
                showPopupMenu(quiz);
                return true;
            });
        }

        private void setCategoryIcon(String category) {
            int iconRes;
            int gradientRes;
            
            if (category == null) {
                iconRes = R.drawable.ic_rocket;
                gradientRes = R.drawable.gradient_science;
            } else {
                switch (category) {
                    case "Science":
                    case "Khoa học":
                        iconRes = R.drawable.ic_rocket;
                        gradientRes = R.drawable.gradient_science;
                        break;
                    case "Geography":
                    case "Địa lý":
                        iconRes = R.drawable.ic_globe;
                        gradientRes = R.drawable.gradient_geography;
                        break;
                    case "Sports":
                    case "Thể thao":
                        iconRes = R.drawable.ic_sports;
                        gradientRes = R.drawable.gradient_sports;
                        break;
                    case "Biology":
                    case "Sinh học":
                        iconRes = R.drawable.ic_tree;
                        gradientRes = R.drawable.gradient_biology;
                        break;
                    case "Technology":
                    case "Công nghệ":
                        iconRes = R.drawable.ic_rocket;
                        gradientRes = R.drawable.gradient_technology;
                        break;
                    case "Networking":
                    case "Mạng máy tính":
                        iconRes = R.drawable.ic_globe;
                        gradientRes = R.drawable.gradient_networking;
                        break;
                    case "Solar System":
                    case "Hệ mặt trời":
                        iconRes = R.drawable.ic_rocket;
                        gradientRes = R.drawable.gradient_solar_system;
                        break;
                    case "Travel":
                    case "Du lịch":
                        iconRes = R.drawable.ic_globe;
                        gradientRes = R.drawable.gradient_travel;
                        break;
                    default:
                        // Default to blue gradient for other categories
                        iconRes = R.drawable.ic_globe;
                        gradientRes = R.drawable.gradient_default;
                        break;
                }
            }
            
            binding.imgCategoryIcon.setImageResource(iconRes);
            binding.viewIconBackground.setBackgroundResource(gradientRes);
        }

        private void setCategoryTextColor(String category) {
            // Set all quiz titles to blue color to match the design
            binding.txtTitle.setTextColor(binding.getRoot().getContext().getColor(R.color.join_quiz_blue));
        }

        private void showPopupMenu(Quiz quiz) {
            android.view.View dialogView = android.view.LayoutInflater.from(binding.getRoot().getContext())
                    .inflate(com.example.studyquizz.R.layout.dialog_quiz_options, null);
            
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(binding.getRoot().getContext())
                    .setView(dialogView)
                    .create();
            
            // Make dialog window transparent for rounded corners
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            // Edit button
            dialogView.findViewById(com.example.studyquizz.R.id.btnEditQuiz).setOnClickListener(v -> {
                listener.onEditQuiz(quiz);
                dialog.dismiss();
            });
            
            // Delete button
            dialogView.findViewById(com.example.studyquizz.R.id.btnDeleteQuiz).setOnClickListener(v -> {
                listener.onDeleteQuiz(quiz);
                dialog.dismiss();
            });
            
            dialog.show();
        }
    }
}


