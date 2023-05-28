package com.anczykowski.parser.structures.expressions;

import java.util.ArrayList;

import com.anczykowski.lexer.Location;
import com.anczykowski.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class ClassInitExpression extends Expression {

    @Getter
    private final String identifier;

    @Getter
    private final ArrayList<Arg> args;

    @Getter
    private Location location = null;

    @Getter
    private String characterBuffer = null;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
