package com.stockmaster.auth.service;

import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.InscriptionGroupeRequest;
import com.stockmaster.auth.dto.request.ForgotPasswordRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.request.RefreshTokenRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;
import com.stockmaster.auth.dto.response.RefreshTokenResponse;

import com.stockmaster.auth.dto.request.ResetPasswordRequest;
import com.stockmaster.auth.dto.request.ChangePasswordRequest;

public interface AuthService {

    InscriptionResponse inscrireEntrepriseUnique(InscriptionEntrepriseUniqueRequest request);

    InscriptionResponse inscrireGroupe(InscriptionGroupeRequest request);

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request);

    void logout();

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
