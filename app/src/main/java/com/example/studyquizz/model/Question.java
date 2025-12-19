package com.example.studyquizz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Question implements Serializable {
    private String id;
    private String content;
    private List<String> options;
    private int correctIndex;
    private QuestionType type;

    public Question() {
        this.id = UUID.randomUUID().toString();
        this.options = new ArrayList<>();
    }

    public Question(String content, List<String> options, int correctIndex, QuestionType type) {
        this();
        this.content = content;
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
        this.correctIndex = correctIndex;
        this.type = type;
    }

    public String getId() {
        if (id == null) id = UUID.randomUUID().toString();
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }
}

