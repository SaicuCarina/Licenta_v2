// ui/login/LoginActivity.java

package com.example.licenta_v2.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.licenta_v2.ui.home.HomeActivity;
import com.example.licenta_v2.R;
import com.example.licenta_v2.ui.register.RegisterActivity;
import com.example.licenta_v2.utils.SharedPrefsHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private CheckBox rememberMeCheckbox;
    private LoginViewModel viewModel;
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.edtEmailAddressLog);
        loginPassword = findViewById(R.id.edtPasswordLog);
        rememberMeCheckbox = findViewById(R.id.chkbRemMe);
        findViewById(R.id.btnLoginLog).setOnClickListener(v -> handleLogin());
        findViewById(R.id.btnRegisterLog).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        prefsHelper = new SharedPrefsHelper(this);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Remember me auto-login
        if (prefsHelper.isRemembered()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }

        viewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                if (rememberMeCheckbox.isChecked()) {
                    prefsHelper.saveCredentials(loginEmail.getText().toString(), loginPassword.getText().toString());
                } else {
                    prefsHelper.clear();
                }
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogin() {
        String email = loginEmail.getText().toString();
        String pass = loginPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Invalid email");
        } else if (pass.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
        } else {
            viewModel.login(email, pass);
        }
    }
}
