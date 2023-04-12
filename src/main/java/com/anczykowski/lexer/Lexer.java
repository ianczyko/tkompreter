package com.anczykowski.lexer;

public interface Lexer {
    @SuppressWarnings("UnusedReturnValue")
    Token getNextToken();

    Token getCurrentToken();
}
