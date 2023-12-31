package com.example.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Builder
@ApiModel(description = "Model of bank statements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statement {

    @ApiModelProperty(notes = "Unique id of bank statement")
    private Long id;
    @ApiModelProperty(notes = "Account number")
    private String accountNumber;
    @ApiModelProperty(notes = "Operation date and time")
    private LocalDateTime operationDate;
    @ApiModelProperty(notes = "Statement beneficiary")
    private String beneficiary;
    @ApiModelProperty(notes = "Statement comment")
    private String comment;
    @ApiModelProperty(notes = "Statement amount")
    private BigDecimal amount;
    @ApiModelProperty(notes = "Statement currency")
    private Currency currency;
}