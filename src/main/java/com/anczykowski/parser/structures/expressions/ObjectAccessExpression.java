package com.anczykowski.parser.structures.expressions;

import com.anczykowski.lexer.Location;
import com.anczykowski.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class ObjectAccessExpression extends Expression {

    @Getter
    private final Expression current;

    @Getter
    private final Expression child;

    @Getter
    private Location location = null;

    @Getter
    private String characterBuffer = null;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
