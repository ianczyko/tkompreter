package com.anczykowski.parser.structures.expressions;

import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AndExpr extends Expression {

    @Getter
    private final Expression left;

    @Getter
    private final Expression right;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
