package com.geekychris.rpncalc.controller;

import com.geekychris.rpncalc.service.SymbolicMathService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/symbolic")
public class SymbolicMathController {

    private final SymbolicMathService symbolicMathService;

    public SymbolicMathController(SymbolicMathService symbolicMathService) {
        this.symbolicMathService = symbolicMathService;
    }

    @PostMapping("/simplify")
    public SymbolicResponse simplify(@Valid @RequestBody SimplifyRequest request) {
        return SymbolicResponse.success(symbolicMathService.simplify(request.expression()));
    }

    @PostMapping("/differentiate")
    public SymbolicResponse differentiate(@Valid @RequestBody DifferentiateRequest request) {
        return SymbolicResponse.success(symbolicMathService.differentiate(request.expression(), request.variable()));
    }
}
