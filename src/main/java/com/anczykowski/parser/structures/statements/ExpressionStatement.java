package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExpressionStatement extends Statement {

    @Getter
    private final Expression expression;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
