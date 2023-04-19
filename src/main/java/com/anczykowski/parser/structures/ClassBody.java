package com.anczykowski.parser.structures;

import java.util.HashMap;

import com.anczykowski.parser.visitors.Visitable;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassBody implements Visitable {
    @Getter
    private final HashMap<String, FuncDef> methods;

    @Getter
    private final HashMap<String, VarStmt> attributes;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
