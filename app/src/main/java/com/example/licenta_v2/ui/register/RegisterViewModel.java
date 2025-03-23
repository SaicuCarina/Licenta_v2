package com.example.licenta_v2.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licenta_v2.repository.AuthRepository;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> registerError = new MutableLiveData<>();

    public void register(String name, String email, String pass, String confirmPass) {
        // La acest punct, datele sunt deja validate în Activity

        authRepository.register(name, email, pass, result -> {
            if (result) {
                registerSuccess.setValue(true);
            } else {
                registerError.setValue("Register failed. Please try again.");
            }
        });
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<String> getRegisterError() {
        return registerError;
    }
}
