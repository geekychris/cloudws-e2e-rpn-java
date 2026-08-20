package com.geekychris.rpncalc.service;

import com.geekychris.rpncalc.exception.RpnEvaluationException;
import com.geekychris.rpncalc.symbolic.BinaryOp;
import com.geekychris.rpncalc.symbolic.Expr;
import com.geekychris.rpncalc.symbolic.Num;
import com.geekychris.rpncalc.symbolic.UnaryOp;
import com.geekychris.rpncalc.symbolic.Var;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parses RPN expressions that may contain variables (e.g. "x y +") into a
 * symbolic expression tree, and offers algebraic simplification and
 * differentiation over that tree.
 */
@Service
public class SymbolicMathService {

    private static final Set<String> BINARY_OPERATORS = Set.of("+", "-", "*", "/", "^");
    private static final Set<String> UNARY_OPERATORS = Set.of("sqrt", "sin", "cos", "tan", "log");
    private static final Pattern VARIABLE_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");

    public String simplify(String expression) {
        return format(simplify(parse(expression)));
    }

    public String differentiate(String expression, String variable) {
        validateVariableName(variable);
        return format(simplify(differentiate(parse(expression), variable)));
    }

    // ---- parsing ----

    Expr parse(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new RpnEvaluationException("Expression must not be empty");
        }

        Deque<Expr> stack = new ArrayDeque<>();
        String[] tokens = expression.trim().split("\\s+");

        for (String token : tokens) {
            if (BINARY_OPERATORS.contains(token)) {
                if (stack.size() < 2) {
                    throw new RpnEvaluationException(
                            "Stack underflow: operator '%s' requires 2 operands but only %d available"
                                    .formatted(token, stack.size()));
                }
                Expr right = stack.pop();
                Expr left = stack.pop();
                stack.push(new BinaryOp(token, left, right));
            } else if (UNARY_OPERATORS.contains(token)) {
                if (stack.isEmpty()) {
                    throw new RpnEvaluationException(
                            "Stack underflow: operator '%s' requires 1 operand but none available".formatted(token));
                }
                stack.push(new UnaryOp(token, stack.pop()));
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

    private Expr parseOperand(String token) {
        try {
            return new Num(Double.parseDouble(token));
        } catch (NumberFormatException e) {
            if (VARIABLE_NAME.matcher(token).matches()) {
                return new Var(token);
            }
            throw new RpnEvaluationException(
                    "Unknown token: '%s' is not a number, variable, or supported operator".formatted(token));
        }
    }

    private void validateVariableName(String variable) {
        if (variable == null || !VARIABLE_NAME.matcher(variable).matches()) {
            throw new RpnEvaluationException(
                    "Variable name must start with a letter and contain only letters or digits");
        }
    }

    // ---- simplification ----

    Expr simplify(Expr expr) {
        if (expr instanceof BinaryOp binaryOp) {
            return simplifyBinary(binaryOp.operator(), simplify(binaryOp.left()), simplify(binaryOp.right()));
        }
        if (expr instanceof UnaryOp unaryOp) {
            return simplifyUnary(unaryOp.operator(), simplify(unaryOp.operand()));
        }
        return expr;
    }

    private Expr simplifyBinary(String operator, Expr left, Expr right) {
        if (left instanceof Num l && right instanceof Num r) {
            return new Num(foldConstants(operator, l.value(), r.value()));
        }

        return switch (operator) {
            case "+" -> {
                if (isZero(left)) yield right;
                if (isZero(right)) yield left;
                if (left.equals(right)) yield new BinaryOp("*", new Num(2), left);
                yield new BinaryOp(operator, left, right);
            }
            case "-" -> {
                if (left.equals(right)) yield new Num(0);
                if (isZero(right)) yield left;
                yield new BinaryOp(operator, left, right);
            }
            case "*" -> {
                if (isZero(left) || isZero(right)) yield new Num(0);
                if (isOne(left)) yield right;
                if (isOne(right)) yield left;
                if (left.equals(right)) yield new BinaryOp("^", left, new Num(2));
                yield new BinaryOp(operator, left, right);
            }
            case "/" -> {
                // Assumes the denominator is not identically zero; a division by an
                // expression that only evaluates to zero for some inputs still simplifies.
                if (isOne(right)) yield left;
                if (isZero(left)) yield new Num(0);
                yield new BinaryOp(operator, left, right);
            }
            case "^" -> {
                if (isZero(right)) yield new Num(1);
                if (isOne(right)) yield left;
                yield new BinaryOp(operator, left, right);
            }
            default -> throw new RpnEvaluationException("Unknown operator: '%s'".formatted(operator));
        };
    }

    private Expr simplifyUnary(String operator, Expr operand) {
        if (operand instanceof Num n) {
            return new Num(foldUnaryConstant(operator, n.value()));
        }
        return new UnaryOp(operator, operand);
    }

    private double foldConstants(String operator, double left, double right) {
        return switch (operator) {
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
    }

    private double foldUnaryConstant(String operator, double operand) {
        return switch (operator) {
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
    }

    private boolean isZero(Expr expr) {
        return expr instanceof Num n && n.value() == 0.0;
    }

    private boolean isOne(Expr expr) {
        return expr instanceof Num n && n.value() == 1.0;
    }

    // ---- differentiation ----

    Expr differentiate(Expr expr, String variable) {
        if (expr instanceof Num) {
            return new Num(0);
        }
        if (expr instanceof Var v) {
            return new Num(v.name().equals(variable) ? 1 : 0);
        }
        if (expr instanceof BinaryOp binaryOp) {
            return differentiateBinary(binaryOp, variable);
        }
        if (expr instanceof UnaryOp unaryOp) {
            return differentiateUnary(unaryOp, variable);
        }
        throw new RpnEvaluationException("Cannot differentiate expression");
    }

    private Expr differentiateBinary(BinaryOp binaryOp, String variable) {
        Expr left = binaryOp.left();
        Expr right = binaryOp.right();
        Expr dLeft = differentiate(left, variable);
        Expr dRight = differentiate(right, variable);

        return switch (binaryOp.operator()) {
            case "+" -> new BinaryOp("+", dLeft, dRight);
            case "-" -> new BinaryOp("-", dLeft, dRight);
            case "*" -> new BinaryOp("+", new BinaryOp("*", dLeft, right), new BinaryOp("*", left, dRight));
            case "/" -> new BinaryOp("/",
                    new BinaryOp("-", new BinaryOp("*", dLeft, right), new BinaryOp("*", left, dRight)),
                    new BinaryOp("^", right, new Num(2)));
            case "^" -> differentiatePower(left, right, dLeft, variable);
            default -> throw new RpnEvaluationException("Unknown operator: '%s'".formatted(binaryOp.operator()));
        };
    }

    private Expr differentiatePower(Expr base, Expr exponent, Expr dBase, String variable) {
        if (exponent instanceof Num n) {
            // Power rule with chain rule: d/dx[base^n] = n * base^(n-1) * d(base)/dx
            Expr reducedPower = new BinaryOp("^", base, new Num(n.value() - 1));
            return new BinaryOp("*", new BinaryOp("*", new Num(n.value()), reducedPower), dBase);
        }
        if (base instanceof Num n && n.value() > 0.0) {
            // Exponential rule: d/dx[a^u] = a^u * ln(a) * d(u)/dx
            Expr dExponent = differentiate(exponent, variable);
            return new BinaryOp("*",
                    new BinaryOp("*", new BinaryOp("^", base, exponent), new Num(Math.log(n.value()))),
                    dExponent);
        }
        throw new RpnEvaluationException(
                "Unsupported differentiation: a variable base raised to a variable exponent is not supported");
    }

    private Expr differentiateUnary(UnaryOp unaryOp, String variable) {
        Expr operand = unaryOp.operand();
        Expr dOperand = differentiate(operand, variable);

        return switch (unaryOp.operator()) {
            case "sqrt" -> new BinaryOp("/", dOperand, new BinaryOp("*", new Num(2), new UnaryOp("sqrt", operand)));
            case "sin" -> new BinaryOp("*", new UnaryOp("cos", operand), dOperand);
            case "cos" -> new BinaryOp("*", new BinaryOp("*", new Num(-1), new UnaryOp("sin", operand)), dOperand);
            case "tan" -> new BinaryOp("*",
                    new BinaryOp("/", new Num(1), new BinaryOp("^", new UnaryOp("cos", operand), new Num(2))),
                    dOperand);
            case "log" -> new BinaryOp("/", dOperand, operand);
            default -> throw new RpnEvaluationException("Unknown operator: '%s'".formatted(unaryOp.operator()));
        };
    }

    // ---- formatting ----

    String format(Expr expr) {
        return formatWithPrecedence(expr, 0);
    }

    private String formatWithPrecedence(Expr expr, int parentPrecedence) {
        if (expr instanceof Num n) {
            return formatNumber(n.value());
        }
        if (expr instanceof Var v) {
            return v.name();
        }
        if (expr instanceof UnaryOp u) {
            return "%s(%s)".formatted(u.operator(), formatWithPrecedence(u.operand(), 0));
        }
        if (expr instanceof BinaryOp b) {
            int precedence = precedenceOf(b.operator());
            boolean rightAssociativitySensitive = b.operator().equals("-") || b.operator().equals("/") || b.operator().equals("^");
            String left = formatWithPrecedence(b.left(), precedence);
            String right = formatWithPrecedence(b.right(), rightAssociativitySensitive ? precedence + 1 : precedence);
            String rendered = "%s %s %s".formatted(left, b.operator(), right);
            return precedence < parentPrecedence ? "(%s)".formatted(rendered) : rendered;
        }
        throw new RpnEvaluationException("Cannot format expression");
    }

    private int precedenceOf(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            default -> throw new RpnEvaluationException("Unknown operator: '%s'".formatted(operator));
        };
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
