package com.anczykowski.parser.structures.expressions;

import java.util.ArrayList;

import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassInitExpression extends Expression {

    @Getter
    private final String identifier;

    @Getter
    private final ArrayList<Arg> args;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}