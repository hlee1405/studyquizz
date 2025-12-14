package com.example.studyquizz.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyquizz.databinding.ItemHistoryBinding;
import com.example.studyquizz.model.QuizResult;
import com.example.studyquizz.ui.QuizResultDetailActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {

    private final List<QuizResult> items = new ArrayList<>();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    // Format cho ngày tháng tiếng Việt: "dd Tháng MM, yyyy"
    private String formatVietnameseDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        
        String day = dayFormat.format(date);
        String month = monthFormat.format(date);
        String year = yearFormat.format(date);
        
        // Chuyển tháng từ số sang tên tháng (1-12)
        int monthNum = Integer.parseInt(month);
        String monthName = getMonthName(monthNum);
        
        return day + " " + monthName + ", " + year;
    }
    
    private String getMonthName(int month) {
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                          "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "Tháng " + month;
    }

    public void submit(List<QuizResult> results) {
        items.clear();
        if (results != null) {
            items.addAll(results);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HistoryHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryBinding binding;

        HistoryHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(QuizResult result) {
            // Quiz title - hiển thị tên quiz thực tế
            binding.txtQuizTitle.setText(result.getQuizTitle());
            
            // Total questions
            binding.txtTotalQuestions.setText("Tổng số câu: " + result.getTotal());
            
            // Correct answers
            binding.txtCorrectAnswers.setText("Số câu đúng: " + result.getCorrectCount());
            
            // Time and Date - hiển thị thời gian làm bài và ngày làm bài
            // Format: "HH:mm, dd Tháng MM, yyyy" (ví dụ: "4:45, 16 Tháng 6, 2025")
            long duration = result.getDuration();
            String timeAndDateText = "";
            
            // Thời gian làm bài (duration)
            if (duration > 0) {
                long totalSeconds = duration / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                timeAndDateText = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
            } else {
                // Fallback: ước tính nếu không có duration (cho dữ liệu cũ)
                int totalSeconds = result.getTotal() * 20;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                timeAndDateText = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
            }
            
            // Thêm ngày làm bài (từ timestamp) với dấu phẩy
            if (result.getTimestamp() > 0) {
                String dateText = formatVietnameseDate(result.getTimestamp());
                timeAndDateText = timeAndDateText + ", " + dateText;
            }
            
            binding.txtTime.setText(timeAndDateText);
            
            // Percentage - calculate from correct/total
            float percentage = 0f;
            if (result.getTotal() > 0) {
                percentage = (result.getCorrectCount() * 100f) / result.getTotal();
            }
            binding.txtPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percentage));
            
            // Set progress bar
            binding.progressBar.setProgress((int) percentage);
            
            // Set click listener to open detail screen
            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), QuizResultDetailActivity.class);
                intent.putExtra(QuizResultDetailActivity.EXTRA_QUIZ_ID, result.getQuizId());
                intent.putExtra(QuizResultDetailActivity.EXTRA_TIMESTAMP, result.getTimestamp());
                v.getContext().startActivity(intent);
            });
        }
    }
}

