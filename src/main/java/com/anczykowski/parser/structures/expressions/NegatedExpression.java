package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NegatedExpression extends Expression {

    @Getter
    private final Expression inner;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
