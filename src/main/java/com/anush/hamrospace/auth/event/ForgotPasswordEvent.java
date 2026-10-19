package com.anush.hamrospace.auth.event;

import java.util.UUID;

public record ForgotPasswordEvent(
    UUID userId,
    String email,
    String fullName,
    String token,
    int exipresAt
) {
    
}
