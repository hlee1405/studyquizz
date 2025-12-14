package com.example.studyquizz.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Quiz {
    private String id;
    private String title;
    private String description;
    private String category;
    private String customCategory;
    private List<Question> questions;
    private int durationMinutes; // Thời gian làm bài (phút)

    public Quiz() {
        this.id = UUID.randomUUID().toString();
        this.questions = new ArrayList<>();
        this.durationMinutes = 15; // Default 15 minutes
    }

    public Quiz(String title, String description, String category, String customCategory) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.customCategory = customCategory;
        this.durationMinutes = 15; // Default 15 minutes
    }

    public String getId() {
        if (id == null) id = UUID.randomUUID().toString();
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCustomCategory() {
        return customCategory;
    }

    public void setCustomCategory(String customCategory) {
        this.customCategory = customCategory;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions == null ? new ArrayList<>() : new ArrayList<>(questions);
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes > 0 ? durationMinutes : 15; // Default to 15 if invalid
    }
}

