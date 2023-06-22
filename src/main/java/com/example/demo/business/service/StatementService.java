package com.example.demo.business.service;

import com.example.demo.business.repository.model.StatementDAO;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface StatementService {

    public void importCSV(MultipartFile file);

    public List<StatementDAO> getFilteredStatements(LocalDate startDate, LocalDate endDate);

    public String generateCSV(List<StatementDAO> filteredStatements);
}