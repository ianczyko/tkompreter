package com.anczykowski.parser.visitors;

public interface Visitable {
    void accept(Visitor visitor);
}
