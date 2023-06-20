package com.example.demo.swagger;

public class DescriptionVariables {

    public static final String NON_NULL_MAX_LONG = "Required non-null value. Starting from 1 to Long.MAX_VALUE";
    public static final String MAX_LONG_RANGE = "range[1, 9223372036854775807]";
    public static final String MODEL_ID_MIN = "Id must be bigger than 0";
    public static final String MODEL_ID_MAX = "Id must be less than 9,223,372,036,854,775,808";
    public static final String BALANCE_MANAGER = "Balance Manager Controller";
    // Key strings for Swagger tags. Used to register controller descriptions
    public static final String MODEL_REGEX_LETTERS_AND_NUMBERS = "Variable must contain letters, numbers, space only";
}