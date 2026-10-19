package com.anush.hamrospace.auth.event;

import java.util.UUID;

public record UserRegisteredEvent (
    UUID userId,
    String email,
    String fullName,
    String token
){
    
}
