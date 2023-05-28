package com.anczykowski.lexer;

public interface Lexer {
    Location getCurrentLocation();

    Location getPreviousLocation();

    @SuppressWarnings("UnusedReturnValue")
    Token getNextToken();

    Token getCurrentToken();

    String getCharacterBuffer();

    String getEffectiveCharacterBuffer();
}
