package com.geekychris.rpncalc.symbolic;

/**
 * A symbolic math expression tree, built when an RPN expression contains
 * variables in addition to (or instead of) numeric literals.
 */
public sealed interface Expr permits Num, Var, BinaryOp, UnaryOp {
}
