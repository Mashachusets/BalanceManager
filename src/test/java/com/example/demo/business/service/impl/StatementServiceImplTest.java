package com.example.demo.business.service.impl;

import com.example.demo.business.customExceptions.CustomExceptions;
import com.example.demo.business.repository.StatementRepository;
import com.example.demo.business.repository.model.StatementDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatementServiceImplTest {

    @InjectMocks
    private StatementServiceImpl statementService;

    @Mock
    private StatementRepository statementRepositoryMock;

    public StatementDAO createStatementDAO(String accountNumber, LocalDateTime operationDate, String beneficiary, String comment, BigDecimal amount, Currency currency) {
        StatementDAO statementDAO = new StatementDAO();
        statementDAO.setAccountNumber(accountNumber);
        statementDAO.setOperationDate(operationDate);
        statementDAO.setBeneficiary(beneficiary);
        statementDAO.setComment(comment);
        statementDAO.setAmount(amount);
        statementDAO.setCurrency(currency);
        return statementDAO;
    }

    @Test
    public void shouldConfirmIsCSVFileTest_Success() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.csv", "text/csv", "csv content".getBytes());
        boolean result = statementService.isCSVFile(mockMultipartFile);
        assertTrue(result);
    }

    @Test
    public void shouldDenyIsCSVFileTest_WrongFormatFile() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "txt content".getBytes());

        assertThrows(CustomExceptions.FileFormatException.class, () -> statementService.isCSVFile(mockMultipartFile));
    }

    @Test
    public void shouldImportCSVTest_Success() {
        String csvContent = "AccountNumber,OperationDate,Beneficiary,Comment,Amount,Currency\n" +
                "123456,2023-06-24 10:30:00,Tom Hanks,Payment,100.00,USD\n" +
                "789012,2023-06-25 15:45:00,Karen Baron,Transfer,250.50,EUR";

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        ArgumentMatcher<List<StatementDAO>> statementListMatcher = argument -> {

            assertEquals(2, argument.size());

            StatementDAO statement1 = argument.get(0);
            assertEquals("123456", statement1.getAccountNumber());
            assertEquals(statement1.getOperationDate(), LocalDateTime.parse("2023-06-24 10:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            assertEquals(statement1.getBeneficiary(), "Tom Hanks");
            assertEquals(statement1.getComment(), "Payment");
            assertEquals(statement1.getAmount(), new BigDecimal("100.00"));
            assertEquals(statement1.getCurrency(), Currency.getInstance("USD"));

            StatementDAO statement2 = argument.get(1);
            assertEquals("789012", statement2.getAccountNumber());
            assertEquals(statement2.getOperationDate(), LocalDateTime.parse("2023-06-25 15:45:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            assertEquals(statement2.getBeneficiary(), "Karen Baron");
            assertEquals(statement2.getComment(), "Transfer");
            assertEquals(statement2.getAmount(), new BigDecimal("250.50"));
            assertEquals(statement2.getCurrency(), Currency.getInstance("EUR"));

            return true;
        };

        statementService.importCSV(mockFile);

        verify(statementRepositoryMock, times(1)).saveAll(argThat(statementListMatcher));
    }

    @Test
    public void shouldNotImportCSVTest_EmptyFile() {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);
        assertThrows(CustomExceptions.EmptyFileException.class, () -> statementService.importCSV(mockFile));
    }

    @Test
    public void shouldNotImportCSVTest_InvalidAmount() {
        String csvContent = "accountNumber,operationDate,beneficiary,comment,amount,currency\n" +
                "123456,2023-06-24 10:00:00,John Doe,Test comment,0,USD";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());
        assertThrows(CustomExceptions.InvalidAmountException.class, () -> statementService.importCSV(mockFile));
    }

    @Test
    public void shouldThrowFileReadExceptionWhenFileReadingFails() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getInputStream()).thenThrow(new IOException("Failed to read the file"));
        assertThrows(CustomExceptions.FileReadException.class, () -> statementService.importCSV(mockFile));
    }

    @Test
    public void shouldGetFilteredStatements_Success() {
        StatementDAO statement1 = new StatementDAO();
        statement1.setAccountNumber("123456");
        statement1.setOperationDate(LocalDateTime.of(2023, 6, 24, 10, 30, 0));

        StatementDAO statement2 = new StatementDAO();
        statement2.setAccountNumber("789012");
        statement2.setOperationDate(LocalDateTime.of(2023, 6, 25, 15, 45, 0));

        List<StatementDAO> statements = Arrays.asList(statement1, statement2);

        LocalDateTime startDateTime = LocalDate.of(2023, 6, 24).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.of(2023, 6, 25).atStartOfDay();
        when(statementRepositoryMock.findByOperationDateBetween(startDateTime, endDateTime)).thenReturn(statements);

        List<StatementDAO> result = statementService.getFilteredStatements(LocalDate.of(2023, 6, 24), LocalDate.of(2023, 6, 25));

        verify(statementRepositoryMock, times(1)).findByOperationDateBetween(startDateTime, endDateTime);
        assertEquals(result,statements);
    }


    @Test
    public void shouldNotGetFilteredStatementsTest_InvalidDateInputExceedsLength() {
        LocalDate invalidDate = LocalDate.of(20233, 6, 24);
        assertThrows(CustomExceptions.InvalidDateInputException.class, () -> statementService.getFilteredStatements(invalidDate, null));
    }

    @Test
    public void shouldNotGetFilteredStatementsTest_InvalidDateInputIncorrectDate() {
        String invalidDate = "2022-66-66";
        assertThrows(DateTimeParseException.class, () -> statementService.getFilteredStatements(LocalDate.parse(invalidDate, DateTimeFormatter.ISO_DATE), null));
    }

    @Test
    public void shouldNotGetFilteredStatementsTest_InvalidDateRange() {
        LocalDate startDate = LocalDate.of(2023, 6, 24);
        LocalDate endDate = LocalDate.of(2021, 6, 25);

        assertThrows(CustomExceptions.InvalidDateRangeException.class, () -> statementService.getFilteredStatements(startDate, endDate));
    }

    @Test
    public void shouldGetFilteredStatementsWithAccountTest_Success() {
        String accountNumber = "123456";
        LocalDate startDate = LocalDate.of(2023, 6, 23);
        LocalDate endDate = LocalDate.of(2023, 6, 25);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay();

        StatementDAO statement1 = new StatementDAO();
        statement1.setAccountNumber(accountNumber);
        statement1.setOperationDate(LocalDateTime.of(2023, 6, 24, 10, 30, 0));

        StatementDAO statement2 = new StatementDAO();
        statement2.setAccountNumber(accountNumber);
        statement2.setOperationDate(LocalDateTime.of(2023, 6, 24, 15, 45, 0));

        List<StatementDAO> statements = Arrays.asList(statement1, statement2);

        when(statementRepositoryMock.findByAccountNumberAndOperationDateBetween(
                eq(accountNumber),
                eq(startDateTime),
                eq(endDateTime)
        )).thenReturn(statements);

        List<StatementDAO> result = statementService.getFilteredStatements(accountNumber, startDate, endDate);

        verify(statementRepositoryMock, times(1)).findByAccountNumberAndOperationDateBetween(
                eq(accountNumber),
                eq(startDateTime),
                eq(endDateTime)
        );

        assertEquals(result, statements);
    }

    @Test
    public void shouldNotGetFilteredStatementsWithAccountTest_EmptyAccountInput() {
        assertThrows(IllegalArgumentException.class, () -> statementService.getFilteredStatements(null, null, null));
    }

    @Test
    public void shouldNotGetFilteredStatementsWithAccountTest_InvalidDateRange2() {
        String accountNumber = "123456789";
        LocalDate startDate = LocalDate.of(2023, 6, 24);
        LocalDate endDate = LocalDate.of(2021, 6, 25);

        assertThrows(CustomExceptions.InvalidDateRangeException.class, () -> statementService.getFilteredStatements(accountNumber, startDate, endDate));
    }

    @Test
    public void shouldNotGetFilteredStatementsWithAccountTest_InvalidDateInputExceedsLength2() {
        String accountNumber = "123456789";
        LocalDate invalidDate = LocalDate.of(20233, 11, 24); // Example of an invalid date

        assertThrows(CustomExceptions.InvalidDateInputException.class, () -> statementService.getFilteredStatements(accountNumber, invalidDate, null));
    }

    @Test
    public void shouldNotGetFilteredStatementsWithAccountTest_InvalidDateInputIncorrectDate2() {
        String invalidDate = "2022-66-66"; // Example of an invalid date format

        assertThrows(DateTimeParseException.class, () -> statementService.getFilteredStatements(LocalDate.parse(invalidDate, DateTimeFormatter.ISO_DATE), null));
    }

    @Test
    public void shouldGetMulticurrencyAmounts_Success() {
        String validAccountNumber = "123456";
        when(statementRepositoryMock.existsStatementByAccountNumber(validAccountNumber)).thenReturn(true);

        LocalDate startDate = LocalDate.of(2021, 6, 23);
        LocalDate endDate = LocalDate.of(2021, 6, 25);

        List<StatementDAO> filteredStatements = new ArrayList<>();

        when(statementService.getFilteredStatements(validAccountNumber, startDate, endDate)).thenReturn(filteredStatements);

        StatementDAO statement1 = createStatementDAO("123456",
                LocalDateTime.parse("2021-06-24 10:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "Dwayne Rock", "You rock", new BigDecimal("100.00"), Currency.getInstance("USD"));
        StatementDAO statement2 = createStatementDAO("123456",
                LocalDateTime.parse("2023-06-24 15:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "Dwayne Rock", "You rock", new BigDecimal("150.00"), Currency.getInstance("EUR"));

        filteredStatements.add(statement1);
        filteredStatements.add(statement2);

        Map<String, BigDecimal> expectedMulticurrencyAmounts = new HashMap<>();

        for (StatementDAO statement : filteredStatements) {
            String currency = String.valueOf(statement.getCurrency());
            BigDecimal amount = expectedMulticurrencyAmounts.getOrDefault(currency, BigDecimal.ZERO);
            amount = amount.add(statement.getAmount());
            expectedMulticurrencyAmounts.put(currency, amount);
        }

        Map<String, BigDecimal> actualMulticurrencyAmounts = statementService.getMulticurrencyAmounts(validAccountNumber, startDate, endDate);

        assertEquals(expectedMulticurrencyAmounts, actualMulticurrencyAmounts);
    }

    @Test
    public void shouldNotGetMulticurrencyAmountsTest_InvalidAccount(){
        String invalidAccount = "540";
        when(statementRepositoryMock.existsStatementByAccountNumber(invalidAccount)).thenReturn(false);
        assertThrows(CustomExceptions.InvalidAccountException.class, () -> statementService.getMulticurrencyAmounts(invalidAccount, null, null));
    }

    @Test
    public void shouldThrowCalculationExceptionWhenFilteringStatementsFails() {
        String validAccountNumber = "123456";
        when(statementRepositoryMock.existsStatementByAccountNumber(validAccountNumber)).thenReturn(true);

        LocalDate startDate = LocalDate.of(2021, 6, 23);
        LocalDate endDate = LocalDate.of(2021, 6, 25);

        when(statementService.getFilteredStatements(validAccountNumber, startDate, endDate))
                .thenThrow(new RuntimeException("Failed to filter statements"));

        assertThrows(CustomExceptions.CalculationException.class, () -> statementService.getMulticurrencyAmounts(validAccountNumber, startDate, endDate));
    }

    @Test
    public void shouldGenerateCSV_Success(){
        List<StatementDAO> statements = new ArrayList<>();

        StatementDAO statement1 = createStatementDAO("123456",
                LocalDateTime.parse("2021-06-24 10:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "Dwayne Rock", "You rock", new BigDecimal("100.00"), Currency.getInstance("USD"));

        StatementDAO statement2 = createStatementDAO("123456",
                LocalDateTime.parse("2023-06-24 15:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "Dwayne Rock", "You rock", new BigDecimal("150.00"), Currency.getInstance("EUR"));

        statements.add(statement1);
        statements.add(statement2);

        String result = statementService.generateCSV(statements);

        String expectedCsvContent = "account_number,operation_date,beneficiary,comment,amount,currency\n"
                + "123456,2021-06-24 10:30:00,Dwayne Rock,You rock,100.00,USD\n"
                + "123456,2023-06-24 15:30:00,Dwayne Rock,You rock,150.00,EUR\n";
        assertEquals(expectedCsvContent, result);
    }

    @Test
    public void shouldNotGenerateCSVTest_NoListFound(){
        List<StatementDAO> filteredStatements = Collections.emptyList();
        assertThrows(CustomExceptions.NoListFoundException.class, () -> statementService.generateCSV(filteredStatements));
    }
}