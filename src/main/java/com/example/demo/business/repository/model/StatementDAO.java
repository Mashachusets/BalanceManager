package com.example.demo.business.repository.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "balance_manager")
public class StatementDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CsvBindByName(column = "account_number")
    private String accountNumber;

    @CsvBindByName(column = "operation_date")
    private LocalDateTime operationDate;

    @CsvBindByName(column = "beneficiary")
    private String beneficiary;

    @CsvBindByName(column = "comment")
    private String comment;

    @CsvBindByName(column = "amount")
    private BigDecimal amount;

    @CsvBindByName(column = "currency")
    private Currency currency;
}