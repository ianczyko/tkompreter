package com.anczykowski.lexer;

import lombok.Getter;


public enum TokenType {
    COMMENT,
    IDENTIFIER,
    NUMBER,
    STRING,
    UNKNOWN,
    EOF,

    // keywords

    VAR_KEYWORD,
    IF_KEYWORD,
    ELSE_KEYWORD,
    AND_KEYWORD,
    OR_KEYWORD,
    WHILE_KEYWORD,
    FOR_KEYWORD,
    RETURN_KEYWORD,
    SWITCH_KEYWORD,
    DEFAULT_KEYWORD,
    CLASS_KEYWORD,
    NEW_KEYWORD,

    // 1-character operators
    PLUS, MINUS, ASSIGNMENT, ASTERISK, SLASH,
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    SEMICOLON, COMMA, PERIOD,
    LT, GT, NEG,

    // 2-character operators
    LE(2), GE(2), NE(2), EQ(2), ARROW(2);

    @Getter
    private int arity = 1;

    TokenType() {
    }

    TokenType(int arity) {
        this.arity = arity;
    }
}
