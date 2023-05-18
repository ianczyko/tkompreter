package com.anczykowski.parser.structures;

import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Parameter implements Visitable {
    @Getter
    private final String name;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}