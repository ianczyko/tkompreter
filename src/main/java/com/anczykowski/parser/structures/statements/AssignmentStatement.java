package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssignmentStatement extends Statement {

    @Getter
    private final Expression lval;

    @Getter
    private final Expression rval;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
