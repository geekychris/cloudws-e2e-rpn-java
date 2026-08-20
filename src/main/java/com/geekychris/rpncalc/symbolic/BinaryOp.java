package com.geekychris.rpncalc.symbolic;

public record BinaryOp(String operator, Expr left, Expr right) implements Expr {
}
