package com.anczykowski.lexer;

import lombok.Getter;

public class FloatToken extends Token {
    public FloatToken(TokenType type, Location location, Float value) {
        super(type, location);
        this.value = value;
    }

    @Getter
    private final Float value;

    @Override
    public String toString() {
        var locationStr = super.location + "\t";
        return locationStr + "FloatToken(type=" + this.type + ", value=[" + this.value + "])";
    }
}
