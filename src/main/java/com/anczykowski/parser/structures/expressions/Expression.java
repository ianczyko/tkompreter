package com.anczykowski.parser.structures.expressions;

import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;

public class Expression implements Visitable {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
