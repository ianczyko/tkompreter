package com.anczykowski.parser.structures.expressions;

import java.util.ArrayList;

import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FunctionCallExpression extends Expression {

    @Getter
    private final String identifier;

    @Getter
    private final ArrayList<Arg> args;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
