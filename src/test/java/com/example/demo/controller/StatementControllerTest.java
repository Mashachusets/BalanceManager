package com.example.demo.controller;

import com.example.demo.business.repository.StatementRepository;
import com.example.demo.business.repository.model.StatementDAO;
import com.example.demo.business.service.StatementService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatementController.class)
public class StatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    StatementRepository statementRepository;

    @MockBean
    private StatementService statementService;

    @InjectMocks
    private StatementController statementController;

    String correctCsvData = "account_number,operation_date,beneficiary,comment,amount,currency\nLT762218756725952400,2023-10-12 11:30:00,Maria,Gift,250,EUR";

    @Test
    public void shouldImportCSVTest_Success() throws Exception {
        MockMultipartFile csvFile = new MockMultipartFile("file", "correctStatements.csv", "text/csv", correctCsvData.getBytes());

        when(statementService.isCSVFile(csvFile)).thenReturn(true);
        doNothing().when(statementService).importCSV(csvFile);

        mockMvc.perform(multipart("/api/import")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));

        verify(statementService).isCSVFile(csvFile);

        verify(statementService).importCSV(csvFile);
    }

    @Test
    public void shouldNotImportCSVTest_IncorrectRequestParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/import")
                        .param("file", "invalid_file"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotImportCSVTest_NoFileUploaded() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/import"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldExportCSVTest_Success() throws Exception {
        statementService = Mockito.mock(StatementService.class);

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2022, 12, 31);
        List<StatementDAO> statements = Collections.singletonList(new StatementDAO());

        statementController = new StatementController();

        Field statementServiceField = StatementController.class.getDeclaredField("statementService");
        statementServiceField.setAccessible(true);
        statementServiceField.set(statementController, statementService);

        mockMvc = MockMvcBuilders.standaloneSetup(statementController).build();

        when(statementService.getFilteredStatements(startDate, endDate)).thenReturn(statements);
        when(statementService.generateCSV(statements)).thenReturn("CSV data");

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/api/export")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String expectedCsvContent = "CSV data";
        String actualCsvContent = response.getContentAsString();
        assertEquals(expectedCsvContent, actualCsvContent);

        assertEquals("text/csv", response.getContentType());
        assertEquals("attachment; filename=\"statements.csv\"", response.getHeader(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    public void shouldNotExportCSVTest_WrongDateInput() throws Exception {
        mockMvc.perform(get("/api/export")
                        .param("startDate", "2023/12/31")
                        .param("endDate", "2024/12/31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldCalculateBalanceTest_Success() throws Exception {
        String accountNumber = "LT762218756725952425";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        Map<String, BigDecimal> expectedResponse = new HashMap<>();
        expectedResponse.put("Currency1", new BigDecimal("100.00"));
        expectedResponse.put("Currency2", new BigDecimal("200.00"));

        when(statementService.getMulticurrencyAmounts(accountNumber, startDate, endDate))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/api/calculate/" + accountNumber)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    JSONObject responseJson = new JSONObject(responseBody);

                    BigDecimal currency1Value = new BigDecimal(responseJson.get("Currency1").toString())
                            .setScale(2, RoundingMode.HALF_UP); // Set the scale to 2 decimal places
                    BigDecimal currency2Value = new BigDecimal(responseJson.get("Currency2").toString())
                            .setScale(2, RoundingMode.HALF_UP); // Set the scale to 2 decimal places

                    assertEquals(expectedResponse.get("Currency1"), currency1Value);
                    assertEquals(expectedResponse.get("Currency2"), currency2Value);
                });
    }

    @Test
    public void shouldNotCalculateBalanceTest_WrongDateInput() throws Exception {
        mockMvc.perform(get("/api/calculate/LT762218756725952425")
                        .param("startDate", "2023/12/31")
                        .param("endDate", "2024/12/31"))
                .andExpect(status().isBadRequest());
    }
}