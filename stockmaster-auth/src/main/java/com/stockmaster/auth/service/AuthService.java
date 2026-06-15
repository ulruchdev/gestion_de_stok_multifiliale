package com.stockmaster.auth.service;

import com.stockmaster.auth.dto.request.InscriptionEntrepriseUniqueRequest;
import com.stockmaster.auth.dto.request.InscriptionGroupeRequest;
import com.stockmaster.auth.dto.request.LoginRequest;
import com.stockmaster.auth.dto.response.InscriptionResponse;
import com.stockmaster.auth.dto.response.LoginResponse;

public interface AuthService {

    InscriptionResponse inscrireEntrepriseUnique(InscriptionEntrepriseUniqueRequest request);

    InscriptionResponse inscrireGroupe(InscriptionGroupeRequest request);

    LoginResponse login(LoginRequest request);
}
