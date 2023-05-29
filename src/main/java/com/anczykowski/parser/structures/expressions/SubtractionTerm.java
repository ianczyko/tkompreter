package com.anczykowski.parser.structures.expressions;

import com.anczykowski.lexer.Location;
import com.anczykowski.visitors.Visitor;

public class SubtractionTerm extends LeftRightExpression {

    public SubtractionTerm(Expression left, Expression right) {
        super(left, right);
    }

    public SubtractionTerm(Expression left, Expression right, Location location, String characterBuffer) {
        super(left, right, location, characterBuffer);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
