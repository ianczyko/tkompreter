package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Arg extends Expression {

    @Getter
    private final Expression argument;

    @Getter
    private final boolean isByReference;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
