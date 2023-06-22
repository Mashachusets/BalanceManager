package com.example.demo.business.service.impl;

import com.example.demo.business.mappers.StatementMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
@Log4j2
@Service
public class StatementServiceImpl implements StatementService {

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private StatementMapper statementMapper;

    @Override
    public void importCSV(MultipartFile file) {
        log.info("Create new Statements by passing: {}", file);
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1) // Skip the header line
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
                statement.setAmount(Long.parseLong(line[4]));
                statement.setCurrency(line[5]);
                statements.add(statement);
            }

            log.info("New Statement saved: {}", statements);
            statementRepository.saveAll(statements);
        } catch (IOException | CsvValidationException e) {
            // Handle IOException
        }
    }

    public List<StatementDAO> getFilteredStatements(LocalDate startDate, LocalDate endDate) {

        LocalDateTime startLocalDateTime = LocalDateTime.MIN;
        LocalDateTime endLocalDateTime = LocalDateTime.MAX;

        if (startDate != null) {
            startLocalDateTime = startDate.atStartOfDay();
        }

        if (endDate != null) {
            endLocalDateTime = endDate.atStartOfDay();
        }

        return statementRepository.findByOperationDateBetween(startLocalDateTime, endLocalDateTime);
    }

    public String generateCSV(List<StatementDAO> statements) {
        StringWriter writer = new StringWriter();

        try (ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            // Write the CSV header
            String[] header = {"account_number", "operation_date", "beneficiary", "comment", "amount", "currency"};
            csvWriter.writeNext(header);

            // Write the CSV data for each statement
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (StatementDAO statement : statements) {
                String operationDate = statement.getOperationDate().format(formatter);
                String[] data = {
                        statement.getAccountNumber(),
                        operationDate,
                        statement.getBeneficiary(),
                        statement.getComment(),
                        String.valueOf(statement.getAmount()),
                        statement.getCurrency()
                };
                csvWriter.writeNext(data);
            }

            csvWriter.flush();
        } catch (IOException e) {
            // Handle IOException
        }

        return writer.toString();
    }
}