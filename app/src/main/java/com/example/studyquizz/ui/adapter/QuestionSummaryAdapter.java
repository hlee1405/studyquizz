package com.example.studyquizz.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyquizz.databinding.ItemQuestionSummaryBinding;
import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.QuestionType;

import java.util.ArrayList;
import java.util.List;

public class QuestionSummaryAdapter extends RecyclerView.Adapter<QuestionSummaryAdapter.QuestionHolder> {

    public interface OnActionListener {
        void onRemove(Question question);
    }

    private final List<Question> items = new ArrayList<>();
    private final OnActionListener listener;

    public QuestionSummaryAdapter(OnActionListener listener) {
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
    public QuestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuestionSummaryBinding binding = ItemQuestionSummaryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new QuestionHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class QuestionHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionSummaryBinding binding;

        QuestionHolder(ItemQuestionSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Question question) {
            binding.txtQuestion.setText(question.getContent());
            binding.txtType.setText(question.getType() == QuestionType.TRUE_FALSE ? "Đúng/Sai" : "Trắc nghiệm");
            if (!question.getOptions().isEmpty() && question.getCorrectIndex() < question.getOptions().size()) {
                binding.txtCorrect.setText(question.getOptions().get(question.getCorrectIndex()));
            } else {
                binding.txtCorrect.setText("");
            }
            binding.btnDelete.setOnClickListener(v -> listener.onRemove(question));
        }
    }
}

