package com.anczykowski.lexer;

import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LexerFiltered implements Lexer {
    final Lexer lexer;

    final Predicate<Token> tokenFilter;

    @Override
    public Location getCurrentLocation() {
        return lexer.getCurrentLocation();
    }

    @Override
    public Location getPreviousLocation() {
        return lexer.getPreviousLocation();
    }

    @Override
    public String getCharacterBuffer() {
        return lexer.getCharacterBuffer();
    }

    @Override
    public String getEffectiveCharacterBuffer() {
        return lexer.getEffectiveCharacterBuffer();
    }

    @Override
    public Token getNextToken() {
        var nextToken = lexer.getNextToken();
        while(tokenFilter.negate().test(nextToken)) {
            nextToken = lexer.getNextToken();
        }
        return nextToken;
    }

    @Override
    public Token getCurrentToken() {
        return lexer.getCurrentToken();
    }
}
