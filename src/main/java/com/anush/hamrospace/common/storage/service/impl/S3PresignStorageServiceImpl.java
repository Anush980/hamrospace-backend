package com.anush.hamrospace.common.storage.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anush.hamrospace.common.storage.dto.request.PresignedUploadRequest;
import com.anush.hamrospace.common.storage.dto.response.PresignedUploadResponse;
import com.anush.hamrospace.common.storage.service.PresignStorageService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class S3PresignStorageServiceImpl implements PresignStorageService {
   
    @Override
    public PresignedUploadResponse presignUpload(PresignedUploadRequest request, String folderPath) {
        
        if(request==null || request.fileName()==null || request.fileName().)
        throw new UnsupportedOperationException("Unimplemented method 'presignUpload'");
    }
    
}
