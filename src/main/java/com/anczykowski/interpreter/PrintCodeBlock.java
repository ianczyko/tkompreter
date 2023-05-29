package com.anczykowski.interpreter;

import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.visitors.Visitor;

public class PrintCodeBlock extends CodeBLock {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
