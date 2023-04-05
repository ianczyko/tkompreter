package com.anczykowski.lexer;

import lombok.Builder;

@Builder
public class Token {
    TokenType type;

    String value;

    Location location;

    public String toString() {
        var locationStr = location != null ? location + "\t" : "";
        if(value == null){
            return locationStr + "Token(type=" + this.type + ")";
        }
        return locationStr + "Token(type=" + this.type + ", value=[" + this.value + "])";
    }
}
