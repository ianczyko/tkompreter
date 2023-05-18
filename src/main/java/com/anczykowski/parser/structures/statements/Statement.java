package com.anczykowski.parser.structures.statements;

import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;

public class Statement implements Visitable {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
