package com.anczykowski.parser.structures;

import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class SwitchLabel implements Visitable {
    @Getter
    private final String label;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
