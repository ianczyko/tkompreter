package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.visitors.Visitor;
import com.anczykowski.parser.structures.expressions.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VarStmt extends Statement {
    @Getter
    private final String name;

    @Getter
    private final Expression initial;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
