package com.anczykowski.parser.structures.statements;

import com.anczykowski.lexer.Location;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class AssignmentStatement extends Statement {

    @Getter
    private final Expression lval;

    @Getter
    private final Expression rval;

    @Getter
    private Location location = null;

    @Getter
    private String characterBuffer = null;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
