package com.anczykowski.lexer;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Token {
    @Getter
    protected final TokenType type;

    @Getter
    protected final Location location;

    public String toString() {
        var locationStr = location + "\t";
        return locationStr + "Token(type=" + this.type + ")";
    }
}
