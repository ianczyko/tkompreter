package com.anczykowski.lexer;

import lombok.Builder;

@Builder
public class Token {
    TokenType type;

    String value;

    public String toString() {
        if(value == null){
            return "Token(type=" + this.type + ")";
        }
        return "Token(type=" + this.type + ", value=[" + this.value + "])";
    }
}
