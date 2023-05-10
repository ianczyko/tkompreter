package com.anczykowski.parser.helpers;

import java.util.Iterator;

import com.anczykowski.lexer.Lexer;
import com.anczykowski.lexer.Location;
import com.anczykowski.lexer.Token;
import com.anczykowski.lexer.TokenType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockLexer implements Lexer {

    private final Iterator<Token> tokensIterator;

    private Token currentToken;

    @Override
    public Location getCurrentLocation() {
        return new Location();
    }

    @Override
    public Token getNextToken() {
        currentToken = tokensIterator.hasNext() ?
            tokensIterator.next() :
            new Token(TokenType.EOF, new Location());
        return currentToken;
    }

    @Override
    public Token getCurrentToken() {
        if(currentToken == null) {
            getNextToken();
        }
        return currentToken;
    }

    @Override
    public String getCharacterBuffer() {
        return "";
    }
}
