package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.anczykowski.lexer.helpers.SourceHelpers;

class LexerTest {

    @Test
    void getBasicToken() {
        // given
        try (var src = SourceHelpers.thereIsSource("abc def")) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.IDENTIFIER);
            assertEquals(lexer.getCurrentToken().value, "abc");
        }
    }

    @Test
    void getBasicTokenLast() {
        // given
        try (var src = SourceHelpers.thereIsSource("abc def")) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.IDENTIFIER);
            assertEquals(lexer.getCurrentToken().value, "def");
        }
    }

    @Test
    void getLT() {
        // given
        try (var src = SourceHelpers.thereIsSource("a<b")) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.LT);
        }
    }

    @Test
    void getLE() {
        // given
        try (var src = SourceHelpers.thereIsSource("a<=b")) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.LE);
        }
    }
}