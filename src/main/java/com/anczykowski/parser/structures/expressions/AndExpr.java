package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;

public class AndExpr extends LeftRightExpression {

    public AndExpr(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
