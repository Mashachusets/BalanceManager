package com.example.demo.controller;

import com.example.demo.business.service.StatementService;
import com.example.demo.model.Statement;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "Balance Manager Controller", description = "Controller used to get and create bank statements")
@Log4j2
@RestController
@RequestMapping("/api")
public class CSVController {

    @Autowired
    private StatementService statementService;

    @PostMapping("/import")
    @ApiOperation(value = "Saves statement database",
            notes = "If provided valid statement, saves it",
            response = Statement.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The statement is successfully saved"),
            @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
            @ApiResponse(code = 401, message = "The request requires user authentication"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The server has not found anything matching the Request-URI"),
            @ApiResponse(code = 500, message = "Server error")}
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> importCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }
        // Process the CSV file
        statementService.importCSV(file);
        return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
    }
}