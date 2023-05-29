package com.anczykowski.parser.structures.expressions;

import com.anczykowski.lexer.Location;
import com.anczykowski.visitors.Visitor;

public class OrExpression extends LeftRightExpression {

    public OrExpression(Expression left, Expression right) {
        super(left, right);
    }

    @SuppressWarnings("unused")
    public OrExpression(Expression left, Expression right, Location location, String characterBuffer) {
        super(left, right, location, characterBuffer);
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
