package com.anczykowski.parser.structures;

import java.util.ArrayList;

import com.anczykowski.parser.visitors.Visitable;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FuncDef implements Visitable {
    @Getter
    private final String name;

    @Getter
    private final ArrayList<Parameter> params;

    @Getter
    private final CodeBLock codeBLock;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
