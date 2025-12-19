package com.example.studyquizz.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.studyquizz.model.Question;
import com.example.studyquizz.model.QuestionType;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImportHelper {
    private ImportHelper() {
    }

    public static List<Question> parseFromDocx(Context context, Uri uri) {
        List<Question> questions = new ArrayList<>();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return questions;
            XWPFDocument document = new XWPFDocument(inputStream);
            List<String> lines = new ArrayList<>();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    String trimmed = text.trim();

                    // Nếu một đáp án trong file DOCX được tô đỏ, coi đó là đáp án đúng
                    // bằng cách chèn dấu "*" để tái sử dụng logic parse hiện tại.
                    boolean hasRedRun = false;
                    for (XWPFRun run : paragraph.getRuns()) {
                        String color = run.getColor();
                        // Lưu ý: với version POI hiện tại, chúng ta chỉ có thể lấy màu
                        // trực tiếp từ run.getColor(). Nếu màu đỏ đến từ style phức tạp
                        // thì thư viện này có thể không đọc được.
                        if (color != null && isRedColor(color)) {
                            hasRedRun = true;
                            break;
                        }
                    }

                    if (hasRedRun && trimmed.matches("^[A-Da-d][\\).].*")) {
                        trimmed = "*" + trimmed;
                    }

                    lines.add(trimmed);
                }
            }
            questions.addAll(parseStructuredLines(lines));
        } catch (IOException e) {
            Log.e("ImportHelper", "DOCX parse error", e);
        }
        return questions;
    }

    public static List<Question> parseFromPdf(Context context, Uri uri) {
        List<Question> questions = new ArrayList<>();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return questions;
            PDFBoxResourceLoader.init(context);
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            questions.addAll(parseStructuredLines(splitToLines(text)));
            document.close();
        } catch (IOException e) {
            Log.e("ImportHelper", "PDF parse error", e);
        }
        return questions;
    }

    private static List<String> splitToLines(String text) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(text.getBytes())))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        }
        return lines;
    }

    /**
     * Best-effort parser: detects lines that start with "Câu", "Question" or end with "?"
     * Options detected when line starts with A/B/C/D, "-" or contains "Đúng"/"Sai".
     * Correct answer hint if line contains "*", "[x]" or "(đúng)".
     */
    private static List<Question> parseStructuredLines(List<String> lines) {
        List<Question> questions = new ArrayList<>();
        String currentQuestion = null;
        List<String> options = new ArrayList<>();
        int correctIndex = -1;

        for (String raw : lines) {
            String line = raw.trim();
            boolean looksLikeQuestion = line.toLowerCase(Locale.ROOT).startsWith("câu") ||
                    line.toLowerCase(Locale.ROOT).startsWith("question") ||
                    line.endsWith("?");
            boolean looksLikeOption = line.matches("^[A-Da-d][\\).].*") || line.startsWith("- ") || line.startsWith("+ ");

            if (looksLikeQuestion) {
                if (currentQuestion != null && !options.isEmpty()) {
                    questions.add(buildQuestion(currentQuestion, options, correctIndex));
                }
                currentQuestion = line.replaceFirst("^[A-Da-d]\\)|\\.", "").trim();
                options = new ArrayList<>();
                correctIndex = -1;
            } else if (looksLikeOption || currentQuestion != null) {
                boolean isCorrect = line.contains("*") || line.contains("[x]") || line.toLowerCase(Locale.ROOT).contains("(đúng)");
                String cleaned = line.replaceFirst("^[A-Da-d][\\).]\\s*", "").replace("*", "").replace("[x]", "").trim();
                options.add(cleaned);
                if (isCorrect) {
                    correctIndex = options.size() - 1;
                }
            }
        }

        if (currentQuestion != null && !options.isEmpty()) {
            questions.add(buildQuestion(currentQuestion, options, correctIndex));
        }

        return questions;
    }

    // Nhận diện màu đỏ trong các run của DOCX (thường lưu dưới dạng "FF0000").
    private static boolean isRedColor(String color) {
        String normalized = color.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        // Các biến thể phổ biến cho màu đỏ
        return normalized.equals("FF0000")          // đỏ thuần
                || normalized.equals("RED")        // tên màu
                || normalized.endsWith("FF0000");  // dạng ARGB
    }

    private static Question buildQuestion(String content, List<String> options, int correctIndex) {
        if (options.size() == 2 &&
                (options.get(0).toLowerCase(Locale.ROOT).contains("đúng") || options.get(0).toLowerCase(Locale.ROOT).contains("true")) &&
                (options.get(1).toLowerCase(Locale.ROOT).contains("sai") || options.get(1).toLowerCase(Locale.ROOT).contains("false"))) {
            return new Question(content, options, correctIndex == -1 ? 0 : correctIndex, QuestionType.TRUE_FALSE);
        }
        return new Question(content, options, correctIndex == -1 ? 0 : correctIndex, QuestionType.MULTIPLE_CHOICE);
    }
}








