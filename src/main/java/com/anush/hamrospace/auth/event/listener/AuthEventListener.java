package com.anush.hamrospace.auth.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.anush.hamrospace.auth.event.EmailVerificationEvent;
import com.anush.hamrospace.auth.event.ForgotPasswordEvent;
import com.anush.hamrospace.auth.event.PasswordChangedEvent;
import com.anush.hamrospace.auth.event.UserRegisteredEvent;
import com.anush.hamrospace.core.email.facade.EmailFacade;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final EmailFacade emailFacade;

    @EventListener
    public void handleForgotPassword(ForgotPasswordEvent event) {
        emailFacade.sendForgotPasswordEmail(
                event.email(),
                event.fullName(),
                event.token());
    }

    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        emailFacade.sendPasswordChangedEmail(
                event.email(),
                event.fullName(),
                event.deviceInfo(),
                event.ipAddress());
    }

    @EventListener
    public void handleVerfiyEmail(EmailVerificationEvent event) {
        emailFacade.sendVerifyEmail(event.email(), event.fullName(), event.token());
    }

    @EventListener
    public void handleWelcomeEmail(UserRegisteredEvent event) {
        emailFacade.sendWelcomeEmail(event.email(), event.fullName(), event.token());
    }
}
