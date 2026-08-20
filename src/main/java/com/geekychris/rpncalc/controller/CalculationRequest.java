package com.geekychris.rpncalc.controller;

import jakarta.validation.constraints.NotBlank;

public record CalculationRequest(@NotBlank(message = "expression must not be blank") String expression) {
}
