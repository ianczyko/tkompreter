package com.anczykowski.parser.structures.expressions.relops;

import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;

public class GeRelOpArg extends Expression {

    @Getter
    private final Expression left;

    @Getter
    private final Expression right;

    public GeRelOpArg(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
