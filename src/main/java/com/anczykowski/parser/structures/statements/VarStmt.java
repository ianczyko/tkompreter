package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VarStmt extends Statement {
    @Getter
    private final String name;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
