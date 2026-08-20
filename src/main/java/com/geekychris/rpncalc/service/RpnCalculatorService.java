package com.geekychris.rpncalc.service;

import com.geekychris.rpncalc.exception.RpnEvaluationException;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Evaluates scientific calculator expressions written in Reverse Polish
 * Notation (postfix notation), e.g. "3 4 +" or "9 sqrt 2 ^".
 */
@Service
public class RpnCalculatorService {

    private static final Set<String> BINARY_OPERATORS = Set.of("+", "-", "*", "/", "^");
    private static final Set<String> UNARY_OPERATORS = Set.of("sqrt", "sin", "cos", "tan", "log");

    public double evaluate(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new RpnEvaluationException("Expression must not be empty");
        }

        Deque<Double> stack = new ArrayDeque<>();
        String[] tokens = expression.trim().split("\\s+");

        for (String token : tokens) {
            if (BINARY_OPERATORS.contains(token)) {
                applyBinaryOperator(stack, token);
            } else if (UNARY_OPERATORS.contains(token)) {
                applyUnaryOperator(stack, token);
            } else {
                stack.push(parseOperand(token));
            }
        }

        if (stack.size() != 1) {
            throw new RpnEvaluationException(
                    "Invalid expression: expected a single result but %d value(s) remained on the stack"
                            .formatted(stack.size()));
        }

        return stack.pop();
    }

    private double parseOperand(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            throw new RpnEvaluationException("Unknown token: '%s' is not a number or a supported operator".formatted(token));
        }
    }

    private void applyBinaryOperator(Deque<Double> stack, String operator) {
        if (stack.size() < 2) {
            throw new RpnEvaluationException(
                    "Stack underflow: operator '%s' requires 2 operands but only %d available"
                            .formatted(operator, stack.size()));
        }

        double right = stack.pop();
        double left = stack.pop();

        double result = switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> {
                if (right == 0.0) {
                    throw new RpnEvaluationException("Division by zero");
                }
                yield left / right;
            }
            case "^" -> Math.pow(left, right);
            default -> throw new RpnEvaluationException("Unknown operator: '%s'".formatted(operator));
        };

        stack.push(result);
    }

    private void applyUnaryOperator(Deque<Double> stack, String operator) {
        if (stack.size() < 1) {
            throw new RpnEvaluationException(
                    "Stack underflow: operator '%s' requires 1 operand but none available".formatted(operator));
        }

        double operand = stack.pop();

        double result = switch (operator) {
            case "sqrt" -> {
                if (operand < 0.0) {
                    throw new RpnEvaluationException("Cannot compute sqrt of a negative number: %s".formatted(operand));
                }
                yield Math.sqrt(operand);
            }
            case "sin" -> Math.sin(operand);
            case "cos" -> Math.cos(operand);
            case "tan" -> Math.tan(operand);
            case "log" -> {
                if (operand <= 0.0) {
                    throw new RpnEvaluationException("Cannot compute log of a non-positive number: %s".formatted(operand));
                }
                yield Math.log(operand);
            }
            default -> throw new RpnEvaluationException("Unknown operator: '%s'".formatted(operator));
        };

        stack.push(result);
    }
}
