package com.anczykowski.parser.structures;

import java.util.ArrayList;

import com.anczykowski.parser.visitors.Visitable;
import com.anczykowski.parser.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CodeBLock implements Visitable {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}