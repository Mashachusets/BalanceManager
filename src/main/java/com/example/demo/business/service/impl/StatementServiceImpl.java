package com.example.demo.business.service.impl;

import com.example.demo.business.repository.StatementRepository;
import com.example.demo.business.repository.model.StatementDAO;
import com.example.demo.business.service.StatementService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Log4j2
@Service
public class StatementServiceImpl implements StatementService {

    @Autowired
    private StatementRepository statementRepository;

    @Override
    public void importCSV(MultipartFile file) {
        log.info("Create new Statements by passing : {}", file);
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<StatementDAO> csvToBean = new CsvToBeanBuilder<StatementDAO>(reader)
                    .withType(StatementDAO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<StatementDAO> statements = csvToBean.parse();

            log.info("New Statement saved: {}", () -> statements);
            statementRepository.saveAll(statements);
        } catch (IOException e) {
            // Handle IOException
        }
    }
}