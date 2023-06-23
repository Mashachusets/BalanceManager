package com.example.demo.business.service;

import com.example.demo.business.repository.model.StatementDAO;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatementService {

    void importCSV(MultipartFile file);

    List<StatementDAO> getFilteredStatements(LocalDate startDate, LocalDate endDate);

    String generateCSV(List<StatementDAO> filteredStatements);

    List<StatementDAO> getFilteredStatements(String accountNumber, LocalDate startDate, LocalDate endDate);

    Map<String, BigDecimal> getMulticurrencyAmounts(String accountNumber, LocalDate startDate, LocalDate endDate);
}