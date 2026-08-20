package com.geekychris.rpncalc.service;

import com.geekychris.rpncalc.exception.RpnEvaluationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SymbolicMathServiceTest {

    private final SymbolicMathService service = new SymbolicMathService();

    @ParameterizedTest
    @CsvSource({
            "'x 0 +', x",
            "'0 x +', x",
            "'x 1 *', x",
            "'1 x *', x",
            "'x 0 *', 0",
            "'0 x *', 0",
            "'x 0 -', x",
            "'x x -', 0",
            "'x 1 /', x",
            "'0 x /', 0",
            "'x 1 ^', x",
            "'x 0 ^', 1",
            "'x x +', 2 * x",
            "'x x *', x ^ 2",
    })
    void simplifiesIdentitiesAndLikeTerms(String expression, String expected) {
        assertThat(service.simplify(expression)).isEqualTo(expected);
    }

    @Test
    void simplifyFoldsConstants() {
        assertThat(service.simplify("2 3 +")).isEqualTo("5");
    }

    @Test
    void simplifyFoldsConstantsWithinLargerExpression() {
        assertThat(service.simplify("x 2 3 + *")).isEqualTo("x * 5");
    }

    @Test
    void simplifyLeavesUnrelatedVariableExpressionAlone() {
        assertThat(service.simplify("x y +")).isEqualTo("x + y");
    }

    @Test
    void simplifyPropagatesDivisionByZero() {
        assertThatThrownBy(() -> service.simplify("2 0 /"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Division by zero");
    }

    @Test
    void differentiatesConstantToZero() {
        assertThat(service.differentiate("5", "x")).isEqualTo("0");
    }

    @Test
    void differentiatesMatchingVariableToOne() {
        assertThat(service.differentiate("x", "x")).isEqualTo("1");
    }

    @Test
    void differentiatesUnrelatedVariableToZero() {
        assertThat(service.differentiate("y", "x")).isEqualTo("0");
    }

    @Test
    void differentiatesSumTermwise() {
        assertThat(service.differentiate("x x +", "x")).isEqualTo("2");
    }

    @Test
    void differentiatesProductWithProductRule() {
        assertThat(service.differentiate("x x *", "x")).isEqualTo("2 * x");
    }

    @Test
    void differentiatesPowerWithPowerRule() {
        assertThat(service.differentiate("x 3 ^", "x")).isEqualTo("3 * x ^ 2");
    }

    @Test
    void differentiatesSine() {
        assertThat(service.differentiate("x sin", "x")).isEqualTo("cos(x)");
    }

    @Test
    void differentiatesCosine() {
        assertThat(service.differentiate("x cos", "x")).isEqualTo("-1 * sin(x)");
    }

    @Test
    void differentiatesNaturalLog() {
        assertThat(service.differentiate("x log", "x")).isEqualTo("1 / x");
    }

    @Test
    void differentiatesSqrt() {
        assertThat(service.differentiate("x sqrt", "x")).isEqualTo("1 / (2 * sqrt(x))");
    }

    @Test
    void rejectsVariableBaseWithVariableExponent() {
        assertThatThrownBy(() -> service.differentiate("x y ^", "x"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Unsupported differentiation");
    }

    @Test
    void rejectsInvalidVariableName() {
        assertThatThrownBy(() -> service.differentiate("x", "3x"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Variable name");
    }

    @Test
    void rejectsEmptyExpression() {
        assertThatThrownBy(() -> service.simplify(""))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    void rejectsUnparseableToken() {
        assertThatThrownBy(() -> service.simplify("3 4x +"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Unknown token");
    }

    @Test
    void rejectsStackUnderflow() {
        assertThatThrownBy(() -> service.simplify("x +"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Stack underflow");
    }

    @Test
    void rejectsTooManyOperandsLeftOnStack() {
        assertThatThrownBy(() -> service.simplify("x y"))
                .isInstanceOf(RpnEvaluationException.class)
                .hasMessageContaining("Invalid expression");
    }
}
