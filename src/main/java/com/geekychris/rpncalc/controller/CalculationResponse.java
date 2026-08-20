package com.geekychris.rpncalc.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalculationResponse(Double result, String error) {

    public static CalculationResponse success(double result) {
        return new CalculationResponse(result, null);
    }

    public static CalculationResponse failure(String error) {
        return new CalculationResponse(null, error);
    }
}
