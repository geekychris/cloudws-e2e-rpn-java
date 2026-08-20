package com.geekychris.rpncalc.controller;

import jakarta.validation.constraints.NotBlank;

public record SimplifyRequest(@NotBlank(message = "expression must not be blank") String expression) {
}
