package com.anczykowski.lexer;

import lombok.Getter;


public enum TokenType {
    WHITESPACE,
    IDENTIFIER,
    KEYWORD,
    NUMBER,
    STRING,
    UNKNOWN,
    INDISTINGUISHABLE,

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

    // TODO: inline in lexer and remove INDISTINGUISHABLE
    public static TokenType matchSimpleTokenType(String currentValue) {
        var firstChar = currentValue.charAt(0);
        Character secondChar = null;
        if (currentValue.length() > 1) {
            secondChar = currentValue.charAt(1);
        }
        return switch (firstChar) {
            case '/' -> TokenType.SLASH;
            case '*' -> TokenType.ASTERISK;
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case ',' -> TokenType.COMMA;
            case '.' -> TokenType.PERIOD;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '%' -> TokenType.PERCENT;
            case ';' -> TokenType.SEMICOLON;
            case '<' -> {
                if (secondChar == null) yield TokenType.INDISTINGUISHABLE;
                yield secondChar == '=' ? TokenType.LE : TokenType.LT;
            }
            case '>' -> {
                if (secondChar == null) yield TokenType.INDISTINGUISHABLE;
                yield secondChar == '=' ? TokenType.GE : TokenType.GT;
            }
            case '=' -> {
                if (secondChar == null) yield TokenType.INDISTINGUISHABLE;
                yield secondChar == '=' ? TokenType.EQ : TokenType.ASSIGNMENT;
            }
            case '!' -> {
                if (secondChar == null) yield TokenType.INDISTINGUISHABLE;
                yield secondChar == '=' ? TokenType.NE : TokenType.NEG;
            }
            default -> TokenType.UNKNOWN;
        };
    }

}
