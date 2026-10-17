package com.anush.hamrospace.common.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anush.hamrospace.common.constants.ApiPath;
import com.anush.hamrospace.common.dto.response.ApiResponse;

@RestController
@RequestMapping(ApiPath.VERSION+ApiPath.HEALTH)
public class HealthController {
    
@GetMapping
public ResponseEntity<ApiResponse<String>> health(){
    return ResponseEntity.ok(ApiResponse.ok("Hamrospace api is running"));
}
}
