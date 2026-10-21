package com.anush.hamrospace.core.email.facade;

import org.springframework.stereotype.Service;

import com.anush.hamrospace.core.config.AppProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailFacade {
    
    private final AppProperties appProperties;
}
