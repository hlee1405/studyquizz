package com.example.studyquizz.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.QuestionType;
import com.example.studyquizz.model.Quiz;
import com.example.studyquizz.model.QuizResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizRepository {
    private static final String PREF_NAME = "study_quiz_store";
    private static final String KEY_QUIZZES = "quizzes";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_CUSTOM_CATEGORIES = "custom_categories";
    private static final String KEY_QUIZ_ID_MAPPING = "quiz_id_mapping";
    private static QuizRepository instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private List<Quiz> quizzes;
    private Map<String, List<QuizResult>> history;
    private List<String> customCategories;
    private Map<String, QuizIdInfo> quizIdMapping; // Maps "XXX XXX XXXX" -> QuizIdInfo

    private QuizRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadData();
    }

    public static QuizRepository getInstance(Context context) {
        if (instance == null) {
            instance = new QuizRepository(context.getApplicationContext());
        }
        return instance;
    }

    public List<Quiz> getQuizzes() {
        return quizzes;
    }

    public Quiz getQuizById(String id) {
        for (Quiz quiz : quizzes) {
            if (quiz.getId().equals(id)) {
                return quiz;
            }
        }
        return null;
    }

    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
        // Add custom category if exists
        if (quiz.getCustomCategory() != null && !quiz.getCustomCategory().isEmpty()) {
            addCustomCategory(quiz.getCustomCategory());
        }
        persist();
    }

    public void updateQuiz(Quiz quiz) {
        for (int i = 0; i < quizzes.size(); i++) {
            if (quizzes.get(i).getId().equals(quiz.getId())) {
                quizzes.set(i, quiz);
                break;
            }
        }
        // Add custom category if exists
        if (quiz.getCustomCategory() != null && !quiz.getCustomCategory().isEmpty()) {
            addCustomCategory(quiz.getCustomCategory());
        }
        persist();
    }

    public void deleteQuiz(String quizId) {
        quizzes.removeIf(quiz -> quiz.getId().equals(quizId));
        persist();
    }

    public void saveResult(QuizResult result) {
        List<QuizResult> list = history.getOrDefault(result.getQuizId(), new ArrayList<>());
        list.add(result);
        history.put(result.getQuizId(), list);
        persist();
    }

    public List<QuizResult> getHistoryForQuiz(String quizId) {
        return history.getOrDefault(quizId, new ArrayList<>());
    }

    public Map<String, List<QuizResult>> getAllHistory() {
        return history;
    }

    private void loadData() {
        Type quizType = new TypeToken<List<Quiz>>() {}.getType();
        quizzes = gson.fromJson(prefs.getString(KEY_QUIZZES, null), quizType);
        if (quizzes == null) {
            quizzes = defaultQuizzes();
        } else {
            for (Quiz quiz : quizzes) {
                if (quiz.getQuestions() == null) {
                    quiz.setQuestions(new ArrayList<>());
                }
                for (Question q : quiz.getQuestions()) {
                    if (q.getOptions() == null) {
                        q.setOptions(new ArrayList<>());
                    }
                }
            }
        }
        Type historyType = new TypeToken<Map<String, List<QuizResult>>>() {}.getType();
        history = gson.fromJson(prefs.getString(KEY_HISTORY, null), historyType);
        if (history == null) {
            history = new HashMap<>();
        }
        
        // Load custom categories
        Type customCategoriesType = new TypeToken<List<String>>() {}.getType();
        customCategories = gson.fromJson(prefs.getString(KEY_CUSTOM_CATEGORIES, null), customCategoriesType);
        if (customCategories == null) {
            customCategories = new ArrayList<>();
        }
        
        // Load quiz ID mapping
        Type mappingType = new TypeToken<Map<String, QuizIdInfo>>() {}.getType();
        quizIdMapping = gson.fromJson(prefs.getString(KEY_QUIZ_ID_MAPPING, null), mappingType);
        if (quizIdMapping == null) {
            quizIdMapping = new HashMap<>();
        }
    }

    private void persist() {
        prefs.edit()
                .putString(KEY_QUIZZES, gson.toJson(quizzes))
                .putString(KEY_HISTORY, gson.toJson(history))
                .putString(KEY_CUSTOM_CATEGORIES, gson.toJson(customCategories))
                .putString(KEY_QUIZ_ID_MAPPING, gson.toJson(quizIdMapping))
                .apply();
    }
    
    public void setQuizIdAndPassword(String quizId, String password, String quizUuid) {
        quizIdMapping.put(quizId, new QuizIdInfo(quizUuid, password));
        persist();
    }
    
    public Quiz findQuizByQuizIdAndPassword(String quizId, String password) {
        QuizIdInfo info = quizIdMapping.get(quizId);
        if (info != null && info.password.equals(password)) {
            return getQuizById(info.quizUuid);
        }
        return null;
    }
    
    private static class QuizIdInfo {
        String quizUuid;
        String password;
        
        QuizIdInfo(String quizUuid, String password) {
            this.quizUuid = quizUuid;
            this.password = password;
        }
    }
    
    private void addCustomCategory(String categoryName) {
        if (!customCategories.contains(categoryName)) {
            customCategories.add(categoryName);
        }
    }
    
    public List<String> getCustomCategories() {
        return new ArrayList<>(customCategories);
    }

    private List<Quiz> defaultQuizzes() {
        List<Quiz> list = new ArrayList<>();

        Quiz sample = new Quiz("Kiểm tra Toán cơ bản", "Phép tính nhanh", "Khoa học", null);
        List<String> opts1 = new ArrayList<>();
        opts1.add("2");
        opts1.add("4");
        opts1.add("6");
        opts1.add("8");
        sample.addQuestion(new Question("1 + 3 = ?", opts1, 1, QuestionType.MULTIPLE_CHOICE));

        List<String> opts2 = new ArrayList<>();
        opts2.add("Đúng");
        opts2.add("Sai");
        sample.addQuestion(new Question("Số nguyên tố đầu tiên là 2", opts2, 0, QuestionType.TRUE_FALSE));
        list.add(sample);

        Quiz itQuiz = new Quiz("Nguyên lý HTTT", "Khái niệm cơ bản", "Mạng máy tính", null);
        List<String> opts3 = new ArrayList<>();
        opts3.add("Bảo mật, Toàn vẹn, Khả dụng");
        opts3.add("Bảo mật, Xác thực, Khả năng truy cập");
        opts3.add("Kiểm soát, Toàn vẹn, Phân tích");
        opts3.add("Không có");
        itQuiz.addQuestion(new Question("Bộ ba CIA gồm những thành phần nào?", opts3, 0, QuestionType.MULTIPLE_CHOICE));
        list.add(itQuiz);

        return list;
    }
}

