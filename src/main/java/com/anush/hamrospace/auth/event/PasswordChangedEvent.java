package com.anush.hamrospace.auth.event;

import java.util.UUID;

public record PasswordChangedEvent(
    UUID userId,
    String email,
    String fullName,
    String deviceInfo,
    String ipAddress,
    String userAgent  
) {
    
}
