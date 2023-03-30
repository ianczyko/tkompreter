package com.anczykowski.lexer;

import lombok.Getter;


public enum TokenType {
    COMMENT,
    IDENTIFIER,
    KEYWORD,
    NUMBER,
    STRING,
    UNKNOWN,

    // 1-character types
    PLUS, MINUS, ASSIGNMENT, ASTERISK, SLASH, PERCENT,
    LPAREN, RPAREN,
    SEMICOLON, COMMA, PERIOD,
    LT, GT, NEG,

    // 2-character types
    LE(2), GE(2), NE(2), EQ(2);

    @Getter
    private int arity = 1;

    TokenType() {
    }

    TokenType(int arity) {
        this.arity = arity;
    }
}
