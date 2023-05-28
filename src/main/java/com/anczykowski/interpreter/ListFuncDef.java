package com.anczykowski.interpreter;

import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.visitors.Visitor;

public class ListFuncDef extends FuncDef {

    public ListFuncDef() {
        super();
        super.requireArgMatch = false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
