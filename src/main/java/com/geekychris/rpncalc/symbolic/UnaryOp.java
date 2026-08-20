package com.geekychris.rpncalc.symbolic;

public record UnaryOp(String operator, Expr operand) implements Expr {
}
