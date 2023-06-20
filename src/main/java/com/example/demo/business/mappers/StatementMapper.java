package com.example.demo.business.mappers;

import com.example.demo.business.repository.model.StatementDAO;
import com.example.demo.model.Statement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatementMapper {

    StatementDAO mapToDAO(Statement statement);

    Statement mapFromDAO(StatementDAO statementDAO);

}