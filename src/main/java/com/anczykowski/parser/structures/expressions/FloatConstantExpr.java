package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FloatConstantExpr extends Expression {

    @Getter
    private final Float value;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
