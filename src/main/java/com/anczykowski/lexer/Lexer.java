package com.anczykowski.lexer;

public interface Lexer {
    Location getCurrentLocation();

    @SuppressWarnings("UnusedReturnValue")
    Token getNextToken();

    Token getCurrentToken();

    String getCharacterBuffer();
}
