package com.example.licenta_v2.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.licenta_v2.R;
import com.example.licenta_v2.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullName, email, password, confirmPassword;
    private Button registerBtn;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.edtFullnameReg);
        email = findViewById(R.id.edtEmailAddressReg);
        password = findViewById(R.id.edtPasswordReg);
        confirmPassword = findViewById(R.id.edtPasswordConfReg);
        registerBtn = findViewById(R.id.btnRegisterReg);
        TextView loginLink = findViewById(R.id.btnLogRegister);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        registerBtn.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String emailText = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            boolean isValid = true;

            // Resetăm erorile vechi
            fullName.setError(null);
            email.setError(null);
            password.setError(null);
            confirmPassword.setError(null);

            if (name.isEmpty()) {
                fullName.setError("Full name cannot be empty");
                isValid = false;
            }

            if (emailText.isEmpty()) {
                email.setError("Email cannot be empty");
                isValid = false;
            } else if (!isValidEmail(emailText)) {
                email.setError("Invalid email address");
                isValid = false;
            }

            if (pass.isEmpty()) {
                password.setError("Password cannot be empty");
                isValid = false;
            } else if (!isStrongPassword(pass)) {
                password.setError("Password must be at least 6 characters and contain a number and special character");
                isValid = false;
            }

            if (!pass.equals(confirmPass)) {
                confirmPassword.setError("Passwords do not match");
                isValid = false;
            }

            if (!isValid) return;

            // verificare daca emailul exista deja
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(emailText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            boolean emailExists = task.getResult().getSignInMethods() != null
                                    && !task.getResult().getSignInMethods().isEmpty();

                            if (emailExists) {
                                email.setError("An account with this email already exists");
                            } else {
                                viewModel.register(name, emailText, pass, confirmPass);
                            }
                        } else {
                            Toast.makeText(this, "Could not verify email. Check your connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        viewModel.getRegisterSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        loginLink.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && email.matches(".*@.*\\.[a-zA-Z]{2,3}$");
    }

    private boolean isStrongPassword(String password) {
        // Cel puțin 6 caractere, o cifră și un caracter special
        Pattern passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[!@#$%^&*()_+=<>?/.,;:'\"\\[\\]{}|`~\\-]).{6,}$");
        return passwordPattern.matcher(password).matches();
    }
}
