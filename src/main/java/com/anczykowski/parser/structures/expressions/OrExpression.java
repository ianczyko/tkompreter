package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;

public class OrExpression extends LeftRightExpression {

    public OrExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
