package com.anczykowski.parser.structures.expressions.relops;

import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.structures.expressions.LeftRightExpression;
import com.anczykowski.parser.visitors.Visitor;

public class NeRelExpr extends LeftRightExpression {

    public NeRelExpr(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
