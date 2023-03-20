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
            assertEquals("abc", lexer.getCurrentToken().value);
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
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().type);
            assertEquals(SourceHelpers.createUnicodeString("ząb"), lexer.getCurrentToken().value);
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
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().type);
            assertEquals(SourceHelpers.createUnicodeString("中国"), lexer.getCurrentToken().value);
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
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().type);
            assertEquals(SourceHelpers.createUnicodeString("你好"), lexer.getCurrentToken().value);
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
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().type);
            assertEquals(SourceHelpers.createUnicodeString("a🚀b"), lexer.getCurrentToken().value);
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
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().type);
            assertEquals("def", lexer.getCurrentToken().value);
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
            assertEquals(TokenType.LT, lexer.getCurrentToken().type);
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
            assertEquals(TokenType.LE, lexer.getCurrentToken().type);
        }
    }
}