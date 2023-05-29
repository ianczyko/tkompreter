package com.anczykowski.parser.structures.statements;

import com.anczykowski.lexer.Location;
import com.anczykowski.visitors.Visitor;
import com.anczykowski.parser.structures.expressions.Expression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class VarStmt extends Statement {
    @Getter
    private final String name;

    @Getter
    private final Expression initial;

    @Getter
    private Location location = null;

    @Getter
    private String characterBuffer = null;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
