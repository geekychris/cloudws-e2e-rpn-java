package com.geekychris.rpncalc.service;

import com.geekychris.rpncalc.exception.RpnEvaluationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class RpnCalculatorServiceTest {

    private final RpnCalculatorService service = new RpnCalculatorService();

    @Test
    void evaluatesSimpleAddition() {
        assertThat(service.evaluate("3 4 +")).isEqualTo(7.0);
    }

    @Test
    void evaluatesExpressionDemonstratingRpnPrecedence() {
        // (3 + 4) * 2 - 5 / 5 in infix -> RPN handles precedence via stack order, no parens needed
        assertThat(service.evaluate("3 4 + 2 * 5 5 / -")).isEqualTo(13.0);
    }

    @Test
    void evaluatesNestedExpressionWithoutParentheses() {
        // 5 * (6 + (2 - 3)) = 25
        assertThat(service.evaluate("5 6 2 3 - + *")).isEqualTo(25.0);
    }

    @ParameterizedTest
    @CsvSource({
            "'2 3 +', 5.0",
            "'5 3 -', 2.0",
            "'4 5 *', 20.0",
            "'10 4 /', 2.5",
            "'2 10 ^', 1024.0",
            "'16 sqrt', 4.0",
            "'0 sin', 0.0",
            "'0 cos', 1.0",
            "'0 tan', 0.0",
    })
    void evaluatesEachSupportedOperator(String expression, double expected) {
        assertThat(service.evaluate(expression)).isCloseTo(expected, within(1e-9));
    }

    @Test
    void evaluatesLogOfE() {
        assertThat(service.evaluate("2.718281828459045 log")).isCloseTo(1.0, within(1e-9));
    }

    @Test
    void evaluatesSingleOperand() {
        assertThat(service.evaluate("42")).isEqualTo(42.0);
    }

    @Test
    void evaluatesNegativeNumbers() {
        assertThat(service.evaluate("-5 3 +")).isEqualTo(-2.0);
    }

    @Test
    void rejectsEmptyExpression() {
        assertThatThrownBy(() -> service.evaluate(""))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    void rejectsBlankExpression() {
        assertThatThrownBy(() -> service.evaluate("   "))
                .isInstanceOf(RpnEvaluationException.class);
    }

    @Test
    void rejectsStackUnderflowForBinaryOperator() {
        assertThatThrownBy(() -> service.evaluate("5 +"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Stack underflow");
    }

    @Test
    void rejectsStackUnderflowForUnaryOperator() {
        assertThatThrownBy(() -> service.evaluate("sqrt"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Stack underflow");
    }

    @Test
    void rejectsDivisionByZero() {
        assertThatThrownBy(() -> service.evaluate("5 0 /"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Division by zero");
    }

    @Test
    void rejectsUnknownOperator() {
        assertThatThrownBy(() -> service.evaluate("3 4 %"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Unknown token");
    }

    @Test
    void rejectsNonNumericToken() {
        assertThatThrownBy(() -> service.evaluate("3 banana +"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Unknown token")
                .hasMessageContaining("banana");
    }

    @Test
    void rejectsTooManyOperandsLeftOnStack() {
        assertThatThrownBy(() -> service.evaluate("1 2 3"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Invalid expression");
    }

    @Test
    void rejectsSqrtOfNegativeNumber() {
        assertThatThrownBy(() -> service.evaluate("-4 sqrt"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void rejectsLogOfNonPositiveNumber() {
        assertThatThrownBy(() -> service.evaluate("0 log"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("non-positive");
    }
}
