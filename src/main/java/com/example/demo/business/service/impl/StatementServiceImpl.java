package com.example.demo.business.service.impl;

import com.example.demo.business.customExceptions.CustomExceptions;
import com.example.demo.business.repository.StatementRepository;
import com.example.demo.business.repository.model.StatementDAO;
import com.example.demo.business.service.StatementService;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Log4j2
@Service
public class StatementServiceImpl implements StatementService {

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private CustomExceptions customExceptions;

    int maxDateStringLength = 10;
    String maxMySqlDateTimeValue = "9999-12-31T23:59:59";

    public boolean isCSVFile(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("text/csv")) {
                log.error("Invalid file format.");
                throw new CustomExceptions.FileFormatException("Invalid file format.");
            }
            return true;
        } catch (Exception e) {
            log.error("Error occurred while checking file format: {}", e.getMessage());
            throw new CustomExceptions.FileFormatException("Error occurred while checking file format.", e);
        }
    }

    @Override
    public void importCSV(MultipartFile file) {
        log.info("Create new Statements by passing: {}", file);
        if (file.isEmpty()) {
            log.error("The file you are importing is empty.");
            throw new CustomExceptions.EmptyFileException("File is empty.");
        }
        try {
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1)
                    .build();

            List<StatementDAO> statements = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                StatementDAO statement = new StatementDAO();
                statement.setAccountNumber(line[0]);
                statement.setOperationDate(LocalDateTime.parse(line[1], formatter));
                statement.setBeneficiary(line[2]);
                statement.setComment(line[3]);
                BigDecimal amount = new BigDecimal(line[4]);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("Importing amount is 0.");
                    throw new CustomExceptions.InvalidAmountException("Amount must be greater than zero.");
                }
                statement.setAmount(amount);
                statement.setCurrency(Currency.getInstance(line[5]));
                statements.add(statement);
            }
            log.info("New Statement saved: {}", statements);
            statementRepository.saveAll(statements);
        } catch (IOException e) {
            log.error("Failed to read the file: {}", e.getMessage());
            throw new CustomExceptions.FileReadException("Failed to read the file: " + e.getMessage());
        } catch (CsvValidationException e) {
            log.error("CSV validation error: {}", e.getMessage());
            throw new CustomExceptions.CsvValidationException("CSV validation error: " + e.getMessage());
        }
    }

    public List<StatementDAO> getFilteredStatements(LocalDate startDate, LocalDate endDate) {
        try {
            if ((String.valueOf(startDate).length() != maxDateStringLength && startDate != null)
                    || (String.valueOf(endDate).length() != maxDateStringLength && endDate != null)) {
                throw new CustomExceptions.InvalidDateInputException("Invalid date input.");
            }
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                log.error("Input date range is invalid.");
                throw new CustomExceptions.InvalidDateRangeException("Invalid date range. Start date must be before end date.");
            }
            LocalDateTime startLocalDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
            LocalDateTime endLocalDateTime = endDate != null ? endDate.atStartOfDay() : LocalDateTime.parse(maxMySqlDateTimeValue);

            return statementRepository.findByOperationDateBetween(startLocalDateTime, endLocalDateTime);
        } catch (DateTimeParseException e) {
            log.error("Input date is invalid. {}", e.getMessage());
            throw new CustomExceptions.InvalidDateInputException("Invalid date input: " + e.getMessage());
        }
    }

    public List<StatementDAO> getFilteredStatements(String accountNumber, LocalDate startDate, LocalDate endDate) {
        if (accountNumber == null) {
            log.error("No input account number was provided.");
            throw new IllegalArgumentException("Input account number is required.");
        }
        try {
            if ((String.valueOf(startDate).length() != maxDateStringLength && startDate != null)
                    || (String.valueOf(endDate).length() != maxDateStringLength && endDate != null)) {
                throw new CustomExceptions.InvalidDateInputException("Invalid date input.");
            }
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                log.error("Input date range is invalid.");
                throw new CustomExceptions.InvalidDateRangeException("Invalid date range. Start date must be before end date.");
            }
            LocalDateTime startLocalDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.MIN;
            LocalDateTime endLocalDateTime = endDate != null ? endDate.atStartOfDay() : LocalDateTime.parse(maxMySqlDateTimeValue);
            return statementRepository.findByAccountNumberAndOperationDateBetween(accountNumber, startLocalDateTime, endLocalDateTime);
        } catch (DateTimeParseException e) {
            log.error("Input date is invalid. {}", e.getMessage());
            throw new CustomExceptions.InvalidDateInputException("Invalid date input: " + e.getMessage());
        }
    }

    public Map<String, BigDecimal> getMulticurrencyAmounts(String accountNumber, LocalDate startDate, LocalDate endDate) {
        if (!(statementRepository.existsStatementByAccountNumber(accountNumber))) {
            log.error("Input account does not exist.");
            throw new CustomExceptions.InvalidAccountException("Account does not exist: " + accountNumber);
        }
        try {
            List<StatementDAO> filteredStatements = getFilteredStatements(accountNumber, startDate, endDate);

            Map<String, BigDecimal> multicurrencyAmounts = new HashMap<>();
            for (StatementDAO statement : filteredStatements) {
                String currency = String.valueOf(statement.getCurrency());
                BigDecimal amount = multicurrencyAmounts.getOrDefault(currency, BigDecimal.ZERO);
                amount = amount.add(statement.getAmount());
                multicurrencyAmounts.put(currency, amount);
            }
            return multicurrencyAmounts;
        }
        catch (Exception e) {
            throw new CustomExceptions.CalculationException("Error occurred while calculating multi currency amounts.", e);
        }
    }

    public String generateCSV(List<StatementDAO> statements) {
        if (statements.isEmpty()) {
            log.error("No statements found.");
            throw new CustomExceptions.NoListFoundException("No statements provided for CSV generation.");
        }
        StringWriter writer = new StringWriter();

        try (ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency"};
            csvWriter.writeNext(header);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (StatementDAO statement : statements) {
                String operationDate = statement.getOperationDate().format(formatter);
                String[] data = {
                        statement.getAccountNumber(),
                        operationDate,
                        statement.getBeneficiary(),
                        statement.getComment(),
                        String.valueOf(statement.getAmount()),
                        String.valueOf(statement.getCurrency())
                };
                csvWriter.writeNext(data);
            }

            csvWriter.flush();
        } catch (IOException e) {
            throw new CustomExceptions.GenerateCsvException("Error occurred while generating CSV.", e);
        }
        return writer.toString();
    }
}