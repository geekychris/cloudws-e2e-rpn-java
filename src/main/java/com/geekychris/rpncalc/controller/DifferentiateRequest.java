package com.geekychris.rpncalc.controller;

import jakarta.validation.constraints.NotBlank;

public record DifferentiateRequest(
        @NotBlank(message = "expression must not be blank") String expression,
        @NotBlank(message = "variable must not be blank") String variable) {
}
