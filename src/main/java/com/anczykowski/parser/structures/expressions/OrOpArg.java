package com.anczykowski.parser.structures.expressions;

import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;

public class OrOpArg extends Expression {

    @Getter
    private final Expression left;

    @Getter
    private final Expression right;

    public OrOpArg(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
