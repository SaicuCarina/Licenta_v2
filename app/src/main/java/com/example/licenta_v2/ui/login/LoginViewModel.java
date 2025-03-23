// ui/login/LoginViewModel.java

package com.example.licenta_v2.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licenta_v2.repository.AuthRepository;

public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    public void login(String email, String password) {
        authRepository.login(email, password, success -> loginSuccess.setValue(success));
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }
}
