package com.anczykowski.lexer;

public enum TokenType {
    COMMENT,
    IDENTIFIER,
    INTEGER_NUMBER,
    FLOAT_NUMBER,
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
    NOT_KEYWORD,

    // 1-character operators
    PLUS, MINUS, ASSIGNMENT, ASTERISK, SLASH,
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    SEMICOLON, COMMA, PERIOD,
    LT, GT, NEG,

    // 2-character operators
    LE, GE, NE, EQ, ARROW
}
