package com.example.studyquizz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studyquizz.MainActivity;
import com.example.studyquizz.R;
import com.example.studyquizz.data.AuthManager;
import com.example.studyquizz.databinding.ActivityProfileBinding;
import com.example.studyquizz.ui.HistoryActivity;
import com.example.studyquizz.ui.MyQuizzesActivity;
import com.example.studyquizz.ui.QuizBuilderActivity;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);
        
        setupViews();
        loadUserInfo();
        setupBottomNavigation();

        // Handle system navigation/gesture insets like MainActivity
        setupWindowInsetsForBottomBar();
    }

    private void setupViews() {
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void setupBottomNavigation() {
        binding.btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        binding.btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            finish();
        });
        binding.fabCreate.setOnClickListener(v -> {
            startActivity(new Intent(this, QuizBuilderActivity.class));
            finish();
        });
        binding.btnTrophy.setOnClickListener(v -> {
            startActivity(new Intent(this, MyQuizzesActivity.class));
            finish();
        });
        binding.btnProfile.setOnClickListener(v -> {
            // Already on profile, do nothing
        });
    }

    private void loadUserInfo() {
        String fullName = authManager.getFullName();
        String email = authManager.getEmail();

        if (!fullName.isEmpty()) {
            binding.txtFullName.setText(fullName);
            binding.txtFullNameValue.setText(fullName);
        } else {
            // Extract name from email if full name is not available
            String userName = email != null && email.contains("@") 
                ? email.substring(0, email.indexOf("@")) 
                : "Người dùng";
            if (!userName.isEmpty()) {
                userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
            }
            binding.txtFullName.setText(userName);
            binding.txtFullNameValue.setText(userName);
        }

        if (!email.isEmpty()) {
            binding.txtEmail.setText(email);
            binding.txtEmailValue.setText(email);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    authManager.logout();
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupWindowInsetsForBottomBar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavLayout, (v, insets) -> {
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets gestures = insets.getInsets(WindowInsetsCompat.Type.systemGestures());

            boolean hasNavBar = navBars.bottom > 0;
            boolean hasGestureBar = gestures.bottom > 0;
            boolean isThreeButton = hasNavBar && navBars.bottom >= dpToPx(20);
            boolean isGesture = !isThreeButton && hasGestureBar && navBars.bottom == 0;

            int paddingBottom;
            if (isThreeButton) {
                // Nâng bar lên khỏi cụm nút hệ thống
                v.setTranslationY(-navBars.bottom);
                paddingBottom = dpToPx(4);
            } else if (isGesture) {
                v.setTranslationY(0);
                paddingBottom = dpToPx(4);
            } else {
                v.setTranslationY(0);
                paddingBottom = Math.max(navBars.bottom, gestures.bottom);
            }

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    paddingBottom
            );

            // Lift the profile card so it does not clash with the bottom bar
            android.view.ViewGroup.MarginLayoutParams cardParams =
                    (android.view.ViewGroup.MarginLayoutParams) binding.cardProfile.getLayoutParams();
            int baseBottomMargin = dpToPx(80); // existing design margin
            cardParams.bottomMargin = baseBottomMargin + paddingBottom + (isThreeButton ? navBars.bottom : 0);
            binding.cardProfile.setLayoutParams(cardParams);

            return insets;
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}

