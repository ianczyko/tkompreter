package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;

public class VarStmt extends Statement {
    @Getter
    private final String name;

    @Getter
    private final Expression initial;

    public VarStmt(String name) {
        this.name = name;
        this.initial = null;

    }public VarStmt(String name, Expression initial) {
        this.name = name;
        this.initial = initial;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
