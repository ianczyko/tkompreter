package com.anczykowski.lexer;

import lombok.Getter;

public class StringToken extends Token {
    public StringToken(TokenType type, Location location, String value) {
        super(type, location);
        this.value = value;
    }

    @Getter
    private final String value;

    @Override
    public String toString() {
        var locationStr = super.location + "\t";
        return locationStr + "StringToken(type=" + this.type + ", value=[" + this.value + "])";
    }
}
