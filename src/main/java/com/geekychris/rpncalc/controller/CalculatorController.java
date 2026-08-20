package com.geekychris.rpncalc.controller;

import com.geekychris.rpncalc.service.RpnCalculatorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculatorController {

    private final RpnCalculatorService calculatorService;

    public CalculatorController(RpnCalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/calculate")
    public CalculationResponse calculate(@Valid @RequestBody CalculationRequest request) {
        double result = calculatorService.evaluate(request.expression());
        return CalculationResponse.success(result);
    }
}
