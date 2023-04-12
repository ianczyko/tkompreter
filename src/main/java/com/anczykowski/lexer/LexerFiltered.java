package com.anczykowski.lexer;

import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LexerFiltered implements Lexer {
    final Lexer lexer;

    final Predicate<Token> commentFilter;

    @Override
    public Token getNextToken() {
        var nextToken = lexer.getNextToken();
        while(commentFilter.negate().test(nextToken)) {
            nextToken = lexer.getNextToken();
        }
        return nextToken;
    }

    @Override
    public Token getCurrentToken() {
        return lexer.getCurrentToken();
    }
}
