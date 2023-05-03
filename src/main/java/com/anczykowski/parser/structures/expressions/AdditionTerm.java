package com.anczykowski.parser.structures.expressions;

import com.anczykowski.parser.visitors.Visitor;

public class AdditionTerm extends LeftRightExpression {

    public AdditionTerm(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
