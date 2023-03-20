package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.anczykowski.lexer.helpers.SourceHelpers;

// TODO: refactor order expected/actual

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
    void getBasicTokenUnicodePolish() {
        // given
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("ząb mądrości"))) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.IDENTIFIER);
            assertEquals(lexer.getCurrentToken().value, SourceHelpers.createUnicodeString("ząb"));
        }
    }

    @Test
    void getBasicTokenUnicodeChinese2Bytes() {
        // given
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("中国 token"))) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.IDENTIFIER);
            assertEquals(lexer.getCurrentToken().value, SourceHelpers.createUnicodeString("中国"));
        }
    }

    @Test
    void getBasicTokenUnicodeChinese3Bytes() {
        // given
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("你好 token"))) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.IDENTIFIER);
            assertEquals(lexer.getCurrentToken().value, SourceHelpers.createUnicodeString("你好"));
        }
    }

    // <I could not find any 4 byte chinese characters that pass the alphanumeric test>

    @Test
    void getBasicTokenUnicodeEmoji() {
        // given
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("a🚀b token"))) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().type, TokenType.IDENTIFIER);
            assertEquals(lexer.getCurrentToken().value, SourceHelpers.createUnicodeString("a🚀b"));
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