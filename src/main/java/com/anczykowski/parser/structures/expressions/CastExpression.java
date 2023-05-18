package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CastExpression extends Expression {

    @Getter
    private final Expression inner;

    @Getter
    private final String type;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
