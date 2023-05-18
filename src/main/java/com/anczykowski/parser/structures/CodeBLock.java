package com.anczykowski.parser.structures;

import java.util.ArrayList;

import com.anczykowski.parser.structures.statements.Statement;
import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CodeBLock implements Visitable {

    @Getter
    ArrayList<Statement> statements;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
