package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringExpression extends Expression {

    @Getter
    private final String value;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
