package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IntegerConstantExpr extends Expression {

    @Getter
    private final Integer value;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
