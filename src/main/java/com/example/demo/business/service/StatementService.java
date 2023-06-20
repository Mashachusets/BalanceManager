package com.example.demo.business.service;

import org.springframework.web.multipart.MultipartFile;

public interface StatementService {

    public void importCSV(MultipartFile file);
}