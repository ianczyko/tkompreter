package com.anczykowski.interpreter;

import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.visitors.Visitor;

public class ListFuncDef extends FuncDef {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
