package com.anczykowski.parser.structures;

import com.anczykowski.parser.visitors.Visitable;
import com.anczykowski.parser.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VarStmt implements Visitable {
    @Getter
    private final String name;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
