package com.anczykowski.parser.structures;

import com.anczykowski.parser.visitors.Visitable;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassDef implements Visitable {
    @Getter
    private final String name;

    @Getter
    private final ClassBody classBody;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
