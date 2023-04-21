package com.anczykowski.parser.structures.expressions;

import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssignmentExpression extends Expression {

    @Getter
    private final Expression lval;

    @Getter
    private final Expression rval;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
