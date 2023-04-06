package com.anczykowski.lexer;

import java.util.Stack;

public class FixedCharacterStack extends Stack<String> {
    private final int maxSize;

    public FixedCharacterStack(int size) {
        super();
        this.maxSize = size;
    }

    @Override
    public String push(String character) {
        while (this.size() >= maxSize) {
            this.remove(0);
        }
        return super.push(character);
    }

    @Override
    public String toString() {
        return String.join("", this);
    }
}
