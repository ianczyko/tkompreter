package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;

public class Statement extends Expression {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
