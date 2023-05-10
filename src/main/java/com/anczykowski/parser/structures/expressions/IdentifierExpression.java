package com.anczykowski.parser.structures.expressions;

import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdentifierExpression extends Expression {

    @Getter
    private final String identifier;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
