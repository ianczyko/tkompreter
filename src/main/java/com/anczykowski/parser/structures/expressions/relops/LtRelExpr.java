package com.anczykowski.parser.structures.expressions.relops;

import com.anczykowski.lexer.Location;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.structures.expressions.LeftRightExpression;
import com.anczykowski.visitors.Visitor;

public class LtRelExpr extends LeftRightExpression {

    public LtRelExpr(Expression left, Expression right) {
        super(left, right);
    }

    public LtRelExpr(Expression left, Expression right, Location location, String characterBuffer) {
        super(left, right, location, characterBuffer);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
