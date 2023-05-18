package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectAccessExpression extends Expression {

    @Getter
    private final Expression current;

    @Getter
    private final Expression child;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
