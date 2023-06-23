package com.example.demo.business.repository;

import com.example.demo.business.repository.model.StatementDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatementRepository extends JpaRepository<StatementDAO, Long> {

    List<StatementDAO> findByOperationDateBetween(LocalDateTime startTimestamp, LocalDateTime endTimestamp);

    List<StatementDAO> findByAccountNumberAndOperationDateBetween(String accountNumber, LocalDateTime startTimestamp, LocalDateTime endTimestamp);

    boolean existsStatementByAccountNumber(String accountNumber);
}