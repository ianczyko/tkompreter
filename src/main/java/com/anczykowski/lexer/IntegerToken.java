package com.anczykowski.lexer;

import lombok.Getter;

public class IntegerToken extends Token {
    public IntegerToken(TokenType type, Location location, Integer value) {
        super(type, location);
        this.value = value;
    }

    @Getter
    private final Integer value;

    @Override
    public String toString() {
        var locationStr = super.location + "\t";
        return locationStr + "IntegerToken(type=" + this.type + ", value=[" + this.value + "])";
    }
}
