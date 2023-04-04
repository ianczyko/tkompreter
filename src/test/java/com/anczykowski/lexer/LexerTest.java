package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.anczykowski.lexer.helpers.SourceHelpers;

// TODO: Edge cases
// TODO: Cases for each token

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
        try (var src = SourceHelpers.thereIsSource("abc cba")) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().type);
            assertEquals("cba", lexer.getCurrentToken().value);
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

    @Test
    void getComment() {
        // given
        try (var src = SourceHelpers.thereIsSource("a //bcd")) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.COMMENT, lexer.getCurrentToken().type);
            assertEquals("bcd", lexer.getCurrentToken().value);
        }
    }

    @Test
    void getCommentEdge() {
        // given
        try (var src = SourceHelpers.thereIsSource("a //")) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.COMMENT, lexer.getCurrentToken().type);
            assertEquals("", lexer.getCurrentToken().value);
        }
    }

    private static Stream<Arguments> expectedKeywordTypes() {
        return Stream.of(
            Arguments.of("var", TokenType.VAR_KEYWORD),
            Arguments.of("if", TokenType.IF_KEYWORD),
            Arguments.of("else", TokenType.ELSE_KEYWORD),
            Arguments.of("and", TokenType.AND_KEYWORD),
            Arguments.of("or", TokenType.OR_KEYWORD),
            Arguments.of("while", TokenType.WHILE_KEYWORD),
            Arguments.of("for", TokenType.FOR_KEYWORD),
            Arguments.of("return", TokenType.RETURN_KEYWORD),
            Arguments.of("switch", TokenType.SWITCH_KEYWORD),
            Arguments.of("def", TokenType.DEFAULT_KEYWORD),
            Arguments.of("class", TokenType.CLASS_KEYWORD),
            Arguments.of("new", TokenType.NEW_KEYWORD)
        );
    }

    @ParameterizedTest
    @MethodSource("expectedKeywordTypes")
    void getKeyword(String keyword, TokenType tokenType) {
        // given
        try (var src = SourceHelpers.thereIsSource(keyword)) {
            var lexer = new Lexer(src);

            // when
            lexer.getNextToken();

            // then
            assertEquals(tokenType, lexer.getCurrentToken().type);
        }
    }
}