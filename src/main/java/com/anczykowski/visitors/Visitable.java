package com.anczykowski.visitors;

public interface Visitable {
    void accept(Visitor visitor);
}
