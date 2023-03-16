package com.anczykowski;

import lombok.Builder;

@Builder
public class Token {
    TokenType type;

    String value;

    public String toString() {
        return "Lexem(type=" + this.type + ", value=[" + this.value + "])";
    }
}
