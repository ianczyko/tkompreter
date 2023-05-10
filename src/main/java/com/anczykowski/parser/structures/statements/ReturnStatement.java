package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReturnStatement extends Statement {

    @Getter
    private final Expression inner;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
