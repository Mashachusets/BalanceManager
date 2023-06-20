package com.example.demo.business.repository;

import com.example.demo.business.repository.model.StatementDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementRepository extends JpaRepository<StatementDAO, Long> {
}