package com.anczykowski.parser.structures.expressions;

import com.anczykowski.lexer.Location;
import com.anczykowski.visitors.Visitor;

public class AdditionTerm extends LeftRightExpression {

    public AdditionTerm(Expression left, Expression right) {
        super(left, right);
    }

    public AdditionTerm(Expression left, Expression right, Location location, String characterBuffer) {
        super(left, right, location, characterBuffer);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
