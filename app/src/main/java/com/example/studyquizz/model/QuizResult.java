package com.example.studyquizz.model;

import java.util.ArrayList;
import java.util.List;

public class QuizResult {
    private String quizId;
    private String quizTitle;
    private int correctCount;
    private int wrongCount;
    private int total;
    private int score;
    private int completionRate;
    private long timestamp;
    private long duration; // Thời gian làm bài thực tế (milliseconds)
    private List<Integer> userAnswers; // Danh sách câu trả lời của người dùng (-1 = chưa trả lời)

    public QuizResult() {
        this.userAnswers = new ArrayList<>();
    }

    public QuizResult(String quizId, String quizTitle, int correctCount, int wrongCount, int total, int score, int completionRate, long timestamp) {
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.total = total;
        this.score = score;
        this.completionRate = completionRate;
        this.timestamp = timestamp;
        this.duration = 0; // Default 0 for backward compatibility
        this.userAnswers = new ArrayList<>();
    }

    public QuizResult(String quizId, String quizTitle, int correctCount, int wrongCount, int total, int score, int completionRate, long timestamp, long duration) {
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.total = total;
        this.score = score;
        this.completionRate = completionRate;
        this.timestamp = timestamp;
        this.duration = duration;
        this.userAnswers = new ArrayList<>();
    }

    public QuizResult(String quizId, String quizTitle, int correctCount, int wrongCount, int total, int score, int completionRate, long timestamp, long duration, List<Integer> userAnswers) {
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.total = total;
        this.score = score;
        this.completionRate = completionRate;
        this.timestamp = timestamp;
        this.duration = duration;
        this.userAnswers = userAnswers != null ? new ArrayList<>(userAnswers) : new ArrayList<>();
    }

    public String getQuizId() {
        return quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public int getTotal() {
        return total;
    }

    public int getScore() {
        return score;
    }

    public int getCompletionRate() {
        return completionRate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<Integer> getUserAnswers() {
        if (userAnswers == null) {
            userAnswers = new ArrayList<>();
        }
        return userAnswers;
    }

    public void setUserAnswers(List<Integer> userAnswers) {
        this.userAnswers = userAnswers != null ? new ArrayList<>(userAnswers) : new ArrayList<>();
    }
}

