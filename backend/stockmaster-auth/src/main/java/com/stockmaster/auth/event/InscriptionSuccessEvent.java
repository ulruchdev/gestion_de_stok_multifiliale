package com.stockmaster.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InscriptionSuccessEvent extends ApplicationEvent {

    private final String email;
    private final String prenom;
    private final String nomEntreprise;

    public InscriptionSuccessEvent(Object source, String email, String prenom, String nomEntreprise) {
        super(source);
        this.email = email;
        this.prenom = prenom;
        this.nomEntreprise = nomEntreprise;
    }
}
