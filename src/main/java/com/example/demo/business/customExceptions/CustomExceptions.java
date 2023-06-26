package com.example.demo.business.customExceptions;

import org.springframework.stereotype.Component;

@Component
public class CustomExceptions {

    public static class FileFormatException extends RuntimeException {
        public FileFormatException(String message) {
            super(message);
        }

        public FileFormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EmptyFileException extends RuntimeException {
        public EmptyFileException(String message) {
            super(message);
        }
    }

    public static class InvalidAmountException extends RuntimeException {
        public InvalidAmountException(String message) {
            super(message);
        }
    }

    public static class FileReadException extends RuntimeException {
        public FileReadException(String message) {
            super(message);
        }
    }

    public static class CsvValidationException extends RuntimeException {
        public CsvValidationException(String message) {
            super(message);
        }
    }

    public static class InvalidDateInputException extends RuntimeException {
        public InvalidDateInputException(String message) {
            super(message);
        }
    }

    public static class InvalidDateRangeException extends RuntimeException {
        public InvalidDateRangeException(String message) {
            super(message);
        }
    }

    public static class CalculationException extends RuntimeException {
        public CalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidAccountException extends RuntimeException {
        public InvalidAccountException(String message) {
            super(message);
        }
    }

    public static class GenerateCsvException extends RuntimeException {
        public GenerateCsvException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NoListFoundException extends RuntimeException {
        public NoListFoundException(String message) {
            super(message);
        }
    }
}