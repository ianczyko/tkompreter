package com.anczykowski.parser.structures;

import java.util.HashMap;

import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Program implements Visitable {
    @Getter
    private final HashMap<String, FuncDef> functions;

    @Getter
    private final HashMap<String, ClassDef> classes;


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
